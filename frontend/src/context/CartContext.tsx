/**
 * Shopping Cart Context
 *
 * Manages the global shopping cart state across the application.
 * Provides functions to add, remove, and update cart items.
 */
import React, { createContext, useContext, useState, useCallback } from "react";
import { ProductResponse } from "@/lib/api";

// Represents a single item in the shopping cart
export interface CartItem {
  product: ProductResponse;
  quantity: number;
}

// Cart context interface with all available operations
interface CartContextType {
  items: CartItem[];
  addToCart: (product: ProductResponse, quantity?: number) => void;
  removeFromCart: (productId: number) => void;
  updateQuantity: (productId: number, quantity: number) => void;
  clearCart: () => void;
  totalItems: number;
  totalPrice: number;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export function CartProvider({ children }: { children: React.ReactNode }) {
  const [items, setItems] = useState<CartItem[]>([]);

  /**
   * Add product to cart or increase quantity if already present
   * Automatically caps quantity at available stock
   */
  const addToCart = useCallback((product: ProductResponse, quantity = 1) => {
    setItems((prev) => {
      const existing = prev.find(
        (item) => item.product.productId === product.productId
      );
      if (existing) {
        // Update quantity for existing item (respecting stock limit)
        return prev.map((item) =>
          item.product.productId === product.productId
            ? {
                ...item,
                quantity: Math.min(item.quantity + quantity, product.stock),
              }
            : item
        );
      }
      // Add new item to cart
      return [
        ...prev,
        { product, quantity: Math.min(quantity, product.stock) },
      ];
    });
  }, []);

  const removeFromCart = useCallback((productId: number) => {
    setItems((prev) =>
      prev.filter((item) => item.product.productId !== productId)
    );
  }, []);

  const updateQuantity = useCallback(
    (productId: number, quantity: number) => {
      if (quantity <= 0) {
        removeFromCart(productId);
        return;
      }
      setItems((prev) =>
        prev.map((item) =>
          item.product.productId === productId
            ? { ...item, quantity: Math.min(quantity, item.product.stock) }
            : item
        )
      );
    },
    [removeFromCart]
  );

  const clearCart = useCallback(() => {
    setItems([]);
  }, []);

  const totalItems = items.reduce((sum, item) => sum + item.quantity, 0);
  const totalPrice = items.reduce(
    (sum, item) => sum + item.product.price * item.quantity,
    0
  );

  return (
    <CartContext.Provider
      value={{
        items,
        addToCart,
        removeFromCart,
        updateQuantity,
        clearCart,
        totalItems,
        totalPrice,
      }}
    >
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error("useCart must be used within a CartProvider");
  }
  return context;
}
