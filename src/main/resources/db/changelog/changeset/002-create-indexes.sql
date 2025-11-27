--liquibase formatted sql

--changeset vhmnts:001-add-items-indexes
CREATE INDEX idx_items_name ON items(name);
CREATE INDEX idx_items_price ON items(price);

--changeset vhmnts:002-add-orders-indexes
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_created_at ON orders(created_at);

--changeset vhmnts:003-add-order-items-indexes
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_item_id ON order_items(item_id);
CREATE INDEX idx_order_items_order_item ON order_items(order_id, item_id);
