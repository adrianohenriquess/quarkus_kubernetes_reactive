INSERT INTO customer(id, name) VALUES (nextval('Customer_SEQ'), 'Debbie Hall');
INSERT INTO customer(id, name) VALUES (nextval('Customer_SEQ'), 'Gary Parmenter');
INSERT INTO customer(id, name) VALUES (nextval('Customer_SEQ'), 'Mary Shoestring');
INSERT INTO customer(id, name) VALUES (nextval('Customer_SEQ'), 'Virginia Mayweather');

INSERT INTO orders(id, customerId, description, total) VALUES (nextval('orders_SEQ'), 1, 'Three seater sofa', 2389.32);
INSERT INTO orders(id, customerId, description, total) VALUES (nextval('orders_SEQ'), 1, 'Curtains', 240.98);
INSERT INTO orders(id, customerId, description, total) VALUES (nextval('orders_SEQ'), 2, 'BBQ Grill', 183.99);
INSERT INTO orders(id, customerId, description, total) VALUES (nextval('orders_SEQ'), 3, '6 seater dining table', 1344.99);
