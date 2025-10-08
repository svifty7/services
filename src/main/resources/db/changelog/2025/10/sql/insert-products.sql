insert into rudagames.products (id, name, description, link)
values (1, 'Мозгобойня', 'Барная викторина', 'mb'),
       (3, 'Туц Туц Quiz', 'Караоке-квиз', 'tuts-tuts'),
       (8, 'Держи Пять!', 'Развлекательная игра', 'high-five'),
       (12, 'Квизмашина', 'Барная викторина', 'quizmachine'),
       (14, 'Квизмашина Классика', 'Барная викторина', 'quizmachine-classic'),
       (16, 'Квизмашина Classic', 'Барная викторина', 'mozgo'),
       (20, 'Дополнительные продукты', 'Барная викторина', NULL),
       (21, 'ТУЦ Лото', 'Караоке-квиз', 'tuts-loto')
on conflict do nothing;
