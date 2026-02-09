INSERT INTO items(name,price,deleted) VALUES
('Item1',100,false),
('Item2',200,false),
('Item3',300,false),
('Item4',400,false);

INSERT INTO orders(user_id,status,total_price,deleted) VALUES
(1,'CONFIRMED',1400,false),
(1,'CONFIRMED',400,false),
(1,'CANCELED',300,false);

INSERT INTO order_items(order_id,item_id,quantity,deleted) VALUES
(1,1,4,false),
(1,2,5,false),
(2,1,1,false),
(2,3,1,false),
(3,3,1,false);


