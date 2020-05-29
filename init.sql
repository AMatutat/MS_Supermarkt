CREATE TABLE category(
id SERIAL PRIMARY KEY NOT NULL,
c_name TEXT NOT NULL 
);

CREATE TABLE article(
    id SERIAL PRIMARY KEY NOT NULL,
    manufacture TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    price FLOAT NOT NULL,
    picture BYTEA,
    stock INTEGER NOT NULL
);

CREATE TABLE article_category(
    articleID INTEGER REFERENCES article(id),
    categoryID INTEGER REFERENCES category(id)
);

CREATE TABLE markt_user(
id INTEGER PRIMARY KEY NOT NULL,
points INTEGER, 
isWorker BOOLEAN NOT NULL
);

CREATE TABLE markt_order(
id SERIAL PRIMARY KEY NOT NULL,
userID INTEGER REFERENCES markt_user(id),
state TEXT NOT NULL,
date DATE NOT NULL
);


CREATE TABLE order_article(
    articleID INTEGER REFERENCES article(id),
    orderID INTEGER REFERENCES markt_order(id),
    number INTEGER NOT NULL
);
CREATE TABLE rating(
id SERIAL PRIMARY KEY NOT NULL,
text TEXT NOT NULL,
rating INTEGER NOT NULL,
userID INTEGER REFERENCES markt_user(id),
articleID INTEGER REFERENCES article(id)
);



INSERT INTO category(c_name)
VALUES 
('Gemuese'),
('Obst'),
('Fleisch'),
('Backwaren'),
('Milchprodukte'),
('Tiernahrung'),
('Haushaltsmittel'),
('Vegetarisch/Vegan'),
('Sonstiges');


INSERT INTO article (manufacture,name,description,price,stock)
VALUES
('Schrott&Teuer','Ziegenkaese 200g','Lecker schmecker Ziegenkaese',1.5,5),
('Schrott&Teuer','Fertig Pizza Salami','Lecker schmecker Pizza',2.5,5),
('Schrott&Teuer','Erdbeer Marmelade 100g','Lecker schmecker Marmelade',0.5,5),
('Schrott&Teuer','Cola 2L','Lecker schmecker Cola',1.0,5);

INSERT INTO markt_user(id,points,isWorker)
VALUES
(-1,500,TRUE),
(-2,1000,FALSE);