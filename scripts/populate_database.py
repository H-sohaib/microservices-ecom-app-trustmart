#!/usr/bin/env python3
"""
Database Population Script for Microservices E-commerce Application

This script:
1. Clears old data from MySQL databases
2. Creates users in Keycloak (without deleting existing ones)
3. Populates products
4. Creates orders associated with specific users

Prerequisites:
- pip install mysql-connector-python requests

Usage:
- Make sure MySQL is running on localhost:3306
- Make sure Keycloak is running on localhost:8080
- Ensure the services have been started at least once to create the tables
- Run: python populate_database.py
"""

import mysql.connector
from mysql.connector import Error
from datetime import datetime, timedelta
import random
import requests
import json

# =============================================================================
# CONFIGURATION
# =============================================================================

# Database configuration
DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': ''  # Update if your MySQL has a password
}

# Keycloak configuration
KEYCLOAK_CONFIG = {
    'url': 'http://localhost:8080',
    'realm': 'trustmart',
    'admin_username': 'admin',  # Keycloak admin username
    'admin_password': 'admin',  # Keycloak admin password
    'client_id': 'admin-cli'
}

# =============================================================================
# SAMPLE DATA
# =============================================================================

# Sample products data
PRODUCTS = [
    {
        'name': 'iPhone 15 Pro',
        'description': 'Apple iPhone 15 Pro with A17 Pro chip, 256GB storage, and titanium design.',
        'price': 999.99,
        'stock': 50
    },
    {
        'name': 'Samsung Galaxy S24 Ultra',
        'description': 'Samsung flagship phone with S Pen, 200MP camera, and Snapdragon 8 Gen 3.',
        'price': 1199.99,
        'stock': 35
    },
    {
        'name': 'MacBook Pro 14"',
        'description': 'Apple MacBook Pro with M3 Pro chip, 18GB RAM, and 512GB SSD.',
        'price': 1999.99,
        'stock': 25
    },
    {
        'name': 'Dell XPS 15',
        'description': 'Premium Windows laptop with Intel Core i7, 16GB RAM, and OLED display.',
        'price': 1499.99,
        'stock': 40
    },
    {
        'name': 'Sony WH-1000XM5',
        'description': 'Premium wireless noise-cancelling headphones with 30-hour battery life.',
        'price': 349.99,
        'stock': 100
    },
    {
        'name': 'AirPods Pro 2',
        'description': 'Apple wireless earbuds with active noise cancellation and spatial audio.',
        'price': 249.99,
        'stock': 150
    },
    {
        'name': 'iPad Pro 12.9"',
        'description': 'Apple iPad Pro with M2 chip, Liquid Retina XDR display, and 256GB storage.',
        'price': 1099.99,
        'stock': 30
    },
    {
        'name': 'PlayStation 5',
        'description': 'Sony PlayStation 5 console with DualSense controller and 825GB SSD.',
        'price': 499.99,
        'stock': 20
    },
    {
        'name': 'Nintendo Switch OLED',
        'description': 'Nintendo Switch with 7" OLED screen and enhanced audio.',
        'price': 349.99,
        'stock': 60
    },
    {
        'name': 'Logitech MX Master 3S',
        'description': 'Premium wireless mouse with MagSpeed scrolling and ergonomic design.',
        'price': 99.99,
        'stock': 200
    },
    {
        'name': 'Mechanical Keyboard Pro',
        'description': 'RGB mechanical keyboard with Cherry MX switches and aluminum frame.',
        'price': 149.99,
        'stock': 80
    },
    {
        'name': 'Apple Watch Series 9',
        'description': 'Apple Watch with S9 chip, always-on display, and advanced health features.',
        'price': 399.99,
        'stock': 70
    },
]

# New users to create (will be added as CLIENT role)
NEW_USERS = [
    {
        'username': 'anass',
        'email': 'john.doe@example.com',
        'firstName': 'John',
        'lastName': 'Doe',
        'password': 'password123'
    },
    {
        'username': 'jane_smith',
        'email': 'jane.smith@example.com',
        'firstName': 'Jane',
        'lastName': 'Smith',
        'password': 'password123'
    },
    {
        'username': 'bob_wilson',
        'email': 'bob.wilson@example.com',
        'firstName': 'Bob',
        'lastName': 'Wilson',
        'password': 'password123'
    },
    {
        'username': 'alice_jones',
        'email': 'alice.jones@example.com',
        'firstName': 'Alice',
        'lastName': 'Jones',
        'password': 'password123'
    },
    {
        'username': 'charlie_brown',
        'email': 'charlie.brown@example.com',
        'firstName': 'Charlie',
        'lastName': 'Brown',
        'password': 'password123'
    }
]

# Command statuses
COMMAND_STATUSES = ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED']

# =============================================================================
# KEYCLOAK FUNCTIONS
# =============================================================================

def get_keycloak_admin_token():
    """Get admin access token from Keycloak."""
    url = f"{KEYCLOAK_CONFIG['url']}/realms/master/protocol/openid-connect/token"

    data = {
        'grant_type': 'password',
        'client_id': KEYCLOAK_CONFIG['client_id'],
        'username': KEYCLOAK_CONFIG['admin_username'],
        'password': KEYCLOAK_CONFIG['admin_password']
    }

    try:
        response = requests.post(url, data=data)
        response.raise_for_status()
        return response.json()['access_token']
    except requests.exceptions.RequestException as e:
        print(f"Error getting Keycloak admin token: {e}")
        return None


def get_existing_users(token):
    """Get list of existing users in the realm."""
    url = f"{KEYCLOAK_CONFIG['url']}/admin/realms/{KEYCLOAK_CONFIG['realm']}/users"
    headers = {'Authorization': f'Bearer {token}'}

    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"Error getting existing users: {e}")
        return []


def get_client_role(token):
    """Get the CLIENT role ID."""
    url = f"{KEYCLOAK_CONFIG['url']}/admin/realms/{KEYCLOAK_CONFIG['realm']}/roles/CLIENT"
    headers = {'Authorization': f'Bearer {token}'}

    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"Error getting CLIENT role: {e}")
        return None


def create_keycloak_user(token, user_data):
    """Create a new user in Keycloak."""
    url = f"{KEYCLOAK_CONFIG['url']}/admin/realms/{KEYCLOAK_CONFIG['realm']}/users"
    headers = {
        'Authorization': f'Bearer {token}',
        'Content-Type': 'application/json'
    }

    user_payload = {
        'username': user_data['username'],
        'email': user_data['email'],
        'firstName': user_data['firstName'],
        'lastName': user_data['lastName'],
        'enabled': True,
        'emailVerified': True,
        'credentials': [{
            'type': 'password',
            'value': user_data['password'],
            'temporary': False
        }]
    }

    try:
        response = requests.post(url, headers=headers, json=user_payload)
        if response.status_code == 201:
            # Get the user ID from the Location header
            location = response.headers.get('Location', '')
            user_id = location.split('/')[-1] if location else None
            return user_id
        elif response.status_code == 409:
            print(f"  User '{user_data['username']}' already exists, skipping...")
            return None
        else:
            print(f"  Failed to create user '{user_data['username']}': {response.text}")
            return None
    except requests.exceptions.RequestException as e:
        print(f"  Error creating user '{user_data['username']}': {e}")
        return None


def assign_role_to_user(token, user_id, role):
    """Assign a realm role to a user."""
    url = f"{KEYCLOAK_CONFIG['url']}/admin/realms/{KEYCLOAK_CONFIG['realm']}/users/{user_id}/role-mappings/realm"
    headers = {
        'Authorization': f'Bearer {token}',
        'Content-Type': 'application/json'
    }

    try:
        response = requests.post(url, headers=headers, json=[role])
        return response.status_code == 204
    except requests.exceptions.RequestException as e:
        print(f"  Error assigning role: {e}")
        return False


def get_user_by_username(token, username):
    """Get user by username."""
    url = f"{KEYCLOAK_CONFIG['url']}/admin/realms/{KEYCLOAK_CONFIG['realm']}/users?username={username}"
    headers = {'Authorization': f'Bearer {token}'}

    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        users = response.json()
        for user in users:
            if user['username'] == username:
                return user
        return None
    except requests.exceptions.RequestException as e:
        print(f"Error getting user: {e}")
        return None


def create_users_in_keycloak():
    """Create new users in Keycloak without deleting existing ones."""
    print("\n--- Creating Users in Keycloak ---")

    token = get_keycloak_admin_token()
    if not token:
        print("Failed to get Keycloak admin token. Skipping user creation.")
        return []

    # Get CLIENT role
    client_role = get_client_role(token)
    if not client_role:
        print("Failed to get CLIENT role. Make sure it exists in Keycloak.")
        return []

    created_users = []

    for user_data in NEW_USERS:
        print(f"Creating user: {user_data['username']}...")
        user_id = create_keycloak_user(token, user_data)

        if user_id:
            # Assign CLIENT role
            if assign_role_to_user(token, user_id, client_role):
                print(f"  ✓ Created and assigned CLIENT role to '{user_data['username']}'")
                created_users.append({
                    'id': user_id,
                    'username': user_data['username']
                })
            else:
                print(f"  ✗ Created user but failed to assign role")
        else:
            # Try to get existing user
            existing_user = get_user_by_username(token, user_data['username'])
            if existing_user:
                created_users.append({
                    'id': existing_user['id'],
                    'username': existing_user['username']
                })

    print(f"\nTotal users available for orders: {len(created_users)}")
    return created_users


# =============================================================================
# DATABASE FUNCTIONS
# =============================================================================

def create_connection(database=None):
    """Create a database connection."""
    try:
        config = DB_CONFIG.copy()
        if database:
            config['database'] = database
        connection = mysql.connector.connect(**config)
        return connection
    except Error as e:
        print(f"Error connecting to MySQL: {e}")
        return None


def clear_all_data():
    """Clear all data from both databases."""
    print("\n--- Clearing All Data ---")

    # Clear commands first (due to foreign key constraints)
    connection = create_connection('commanddb')
    if connection:
        try:
            cursor = connection.cursor()
            cursor.execute("SET FOREIGN_KEY_CHECKS = 0")
            cursor.execute("DELETE FROM command_items")
            cursor.execute("DELETE FROM commands")
            cursor.execute("SET FOREIGN_KEY_CHECKS = 1")
            connection.commit()
            print("✓ Cleared all data from commanddb")
        except Error as e:
            print(f"✗ Error clearing commanddb: {e}")
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()

    # Clear products
    connection = create_connection('productdb')
    if connection:
        try:
            cursor = connection.cursor()
            cursor.execute("DELETE FROM products")
            connection.commit()
            print("✓ Cleared all data from productdb")
        except Error as e:
            print(f"✗ Error clearing productdb: {e}")
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()


def populate_products():
    """Populate the products table."""
    print("\n--- Populating Products ---")

    connection = create_connection('productdb')
    if not connection:
        print("Failed to connect to productdb")
        return False

    try:
        cursor = connection.cursor()

        # Insert products
        insert_query = """
            INSERT INTO products (name, description, price, stock)
            VALUES (%s, %s, %s, %s)
        """

        for product in PRODUCTS:
            cursor.execute(insert_query, (
                product['name'],
                product['description'],
                product['price'],
                product['stock']
            ))

        connection.commit()
        print(f"✓ Inserted {len(PRODUCTS)} products into productdb")
        return True

    except Error as e:
        print(f"✗ Error populating products: {e}")
        return False

    finally:
        if connection.is_connected():
            cursor.close()
            connection.close()


def get_products_from_db():
    """Get all products from the database."""
    connection = create_connection('productdb')
    if not connection:
        return []

    try:
        cursor = connection.cursor(dictionary=True)
        cursor.execute("SELECT product_id, name, price FROM products")
        products = cursor.fetchall()
        return products
    except Error as e:
        print(f"Error fetching products: {e}")
        return []
    finally:
        if connection.is_connected():
            cursor.close()
            connection.close()


def populate_commands(users):
    """Populate the commands and command_items tables with user associations."""
    print("\n--- Populating Commands ---")

    if not users:
        print("No users available. Skipping command population.")
        return False

    connection = create_connection('commanddb')
    if not connection:
        print("Failed to connect to commanddb")
        return False

    try:
        cursor = connection.cursor()

        # Get products to reference in commands
        products = get_products_from_db()
        if not products:
            print("No products found. Please populate products first.")
            return False

        # Generate sample commands for each user
        orders_per_user = 3
        total_commands = 0

        for user in users:
            for _ in range(orders_per_user):
                # Random date within the last 30 days
                days_ago = random.randint(0, 30)
                command_date = datetime.now() - timedelta(days=days_ago)

                # Random status (more likely to be completed for older orders)
                if days_ago > 20:
                    status = random.choice(['DELIVERED', 'DELIVERED', 'CANCELLED'])
                elif days_ago > 10:
                    status = random.choice(['SHIPPED', 'DELIVERED', 'PROCESSING'])
                elif days_ago > 5:
                    status = random.choice(['PROCESSING', 'CONFIRMED', 'SHIPPED'])
                else:
                    status = random.choice(['PENDING', 'CONFIRMED', 'PROCESSING'])

                # Calculate total price based on items
                num_items = random.randint(1, 3)
                selected_products = random.sample(products, min(num_items, len(products)))

                items_data = []
                total_price = 0

                for product in selected_products:
                    quantity = random.randint(1, 2)
                    item_price = float(product['price'])
                    total_price += item_price * quantity
                    items_data.append({
                        'product_id': product['product_id'],
                        'quantity': quantity,
                        'price': item_price
                    })

                # Insert command with user info
                insert_command = """
                    INSERT INTO commands (date, status, total_price, user_id, username)
                    VALUES (%s, %s, %s, %s, %s)
                """
                cursor.execute(insert_command, (
                    command_date,
                    status,
                    round(total_price, 2),
                    user['id'],
                    user['username']
                ))
                command_id = cursor.lastrowid

                # Insert command items
                insert_item = """
                    INSERT INTO command_items (command_id, product_id, quantity, price)
                    VALUES (%s, %s, %s, %s)
                """
                for item in items_data:
                    cursor.execute(insert_item, (
                        command_id,
                        item['product_id'],
                        item['quantity'],
                        item['price']
                    ))

                total_commands += 1

        connection.commit()
        print(f"✓ Inserted {total_commands} commands for {len(users)} users into commanddb")
        return True

    except Error as e:
        print(f"✗ Error populating commands: {e}")
        return False

    finally:
        if connection.is_connected():
            cursor.close()
            connection.close()


def show_summary():
    """Show a summary of the data in the databases."""
    print("\n" + "=" * 60)
    print("DATABASE SUMMARY")
    print("=" * 60)

    # Products summary
    connection = create_connection('productdb')
    if connection:
        try:
            cursor = connection.cursor()
            cursor.execute("SELECT COUNT(*) FROM products")
            count = cursor.fetchone()[0]
            print(f"\n📦 Products in productdb: {count}")

            cursor.execute("SELECT name, price, stock FROM products LIMIT 5")
            print("\nSample products:")
            for row in cursor.fetchall():
                print(f"  • {row[0]}: ${row[1]} (Stock: {row[2]})")
        except Error as e:
            print(f"Error reading productdb: {e}")
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()

    # Commands summary
    connection = create_connection('commanddb')
    if connection:
        try:
            cursor = connection.cursor()
            cursor.execute("SELECT COUNT(*) FROM commands")
            count = cursor.fetchone()[0]
            print(f"\n📋 Commands in commanddb: {count}")

            cursor.execute("""
                SELECT c.command_id, c.username, c.status, c.total_price, COUNT(ci.id) as items
                FROM commands c
                LEFT JOIN command_items ci ON c.command_id = ci.command_id
                GROUP BY c.command_id
                ORDER BY c.date DESC
                LIMIT 5
            """)
            print("\nRecent orders:")
            for row in cursor.fetchall():
                print(f"  • Order #{row[0]} by {row[1]}: {row[2]} - ${row[3]} ({row[4]} items)")

            # Orders per user
            cursor.execute("""
                SELECT username, COUNT(*) as order_count
                FROM commands
                GROUP BY username
                ORDER BY order_count DESC
            """)
            print("\nOrders per user:")
            for row in cursor.fetchall():
                print(f"  • {row[0]}: {row[1]} orders")

        except Error as e:
            print(f"Error reading commanddb: {e}")
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()

    print("\n" + "=" * 60)


# =============================================================================
# MAIN FUNCTION
# =============================================================================

def main():
    """Main function to run the population script."""
    print("=" * 60)
    print("E-commerce Database Population Script")
    print("=" * 60)

    print("\nThis script will:")
    print("  1. Clear all existing data from MySQL databases")
    print("  2. Create new users in Keycloak (without deleting existing ones)")
    print("  3. Populate products")
    print("  4. Create orders associated with users")

    print("\nOptions:")
    print("1. Run full population (clear + create users + populate)")
    print("2. Only populate databases (skip Keycloak user creation)")
    print("3. Only create Keycloak users")
    print("4. Show database summary only")
    print("5. Exit")

    choice = input("\nEnter your choice (1-5): ").strip()

    if choice == '1':
        # Full population
        clear_all_data()
        users = create_users_in_keycloak()
        populate_products()
        populate_commands(users)
        show_summary()

    elif choice == '2':
        # Only populate databases (get existing users from Keycloak)
        token = get_keycloak_admin_token()
        users = []
        if token:
            for user_data in NEW_USERS:
                user = get_user_by_username(token, user_data['username'])
                if user:
                    users.append({'id': user['id'], 'username': user['username']})

        if not users:
            print("No users found. Creating users first...")
            users = create_users_in_keycloak()

        clear_all_data()
        populate_products()
        populate_commands(users)
        show_summary()

    elif choice == '3':
        # Only create Keycloak users
        create_users_in_keycloak()

    elif choice == '4':
        show_summary()

    elif choice == '5':
        print("Goodbye!")

    else:
        print("Invalid choice. Running full population...")
        clear_all_data()
        users = create_users_in_keycloak()
        populate_products()
        populate_commands(users)
        show_summary()


if __name__ == "__main__":
    main()

