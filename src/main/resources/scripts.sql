// Получить всех студентов, возраст которых находится между 10 и 35
select * from student where age > 10 and age < 35

// Получить всех студентов, но отобразить только список их имен
select name from student

// Получить всех студентов, у которых в имени присутствует буква A
select * from student where lower(name) like lower('%A%')

// Получить всех студентов, у которых возраст меньше идентификатора
select * from student where age < id

// Получить всех студентов упорядоченных по возрасту
select * from student order by age