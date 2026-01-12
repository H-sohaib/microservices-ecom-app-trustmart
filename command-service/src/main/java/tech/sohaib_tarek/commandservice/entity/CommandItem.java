package tech.sohaib_tarek.commandservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "command_items")
public class CommandItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "command_id", nullable = false)
    @JsonBackReference
    private Command command;

    public CommandItem() {
    }

    public CommandItem(Long id, Long productId, Integer quantity, BigDecimal price, Command command) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.command = command;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public static CommandItemBuilder builder() {
        return new CommandItemBuilder();
    }

    public static class CommandItemBuilder {
        private Long id;
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
        private Command command;

        public CommandItemBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public CommandItemBuilder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        public CommandItemBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public CommandItemBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public CommandItemBuilder command(Command command) {
            this.command = command;
            return this;
        }

        public CommandItem build() {
            return new CommandItem(id, productId, quantity, price, command);
        }
    }
}

