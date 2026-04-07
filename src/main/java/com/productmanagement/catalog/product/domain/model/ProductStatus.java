package com.productmanagement.catalog.product.domain.model;

import com.productmanagement.catalog.product.domain.exception.InvalidProductStateTransitionException;

public enum ProductStatus {
    ACTIVE {
        public void ensureModifiable() {
        }

        public ProductStatus activate() {
            throw new InvalidProductStateTransitionException();
        }

        public ProductStatus deactivate() {
            return INACTIVE;
        }
    }, INACTIVE {
        public void ensureModifiable() {
            throw new InvalidProductStateTransitionException();
        }

        public ProductStatus activate() {
            return ACTIVE;
        }

        public ProductStatus deactivate() {
            throw new InvalidProductStateTransitionException();
        }
    };

    public abstract void ensureModifiable();

    public abstract ProductStatus activate();

    public abstract ProductStatus deactivate();

}
