Lib Module
------------
##### ISO-8583 Java Lib #####
###### Что из себя представляет: ######
За основу взята библиотека с GitHub https://github.com/imohsenb/ISO8583-Message-Client-java.  
Адаптирована под спецификацию ПВУ.
###### Функции: ######
- предоставляет сущность ISO-сообщения и другие, необходимых для работы с ним согласно спецификации ПВУ
- предоставляет функционал для формирования ISO-сообщения и работы с его параметрами (например, для получения параметров поля)

Models Module
-------------
##### Функции: #####
Представление моделей, необходимых для обработки сообщения внутри эмулятора.
##### Связи с другими модулями: #####
Используется в
- Parse Module
- Check Module
- Services Module
- Routing Module
##### Encoded Message Model #####
###### Что из себя представляет: ######
Поле объекта хранит закодированное по определенным правилам ISO-сообщение (16 CC).
###### Функции: ######  
Служит для передачи зашифрованного ISO-сообщения между эмуляторами составляющих ПС и внешними сервисами.
##### Parsed Message Model #####
###### Что из себя представляет: ######
- идентификатор, присваиваемый базой данных
- номер транзакции, присвоенный внешним сервисом, от которого сообщение поступило в ПС
- дату и время поступления сообщения в эмулятор
- исходное зашифрованное сообщение - строка hex (эквивалентна строке, хранящейся в Encoded Message)
- mti
- поля сообщения
- логическую метку о том, были ли внесены изменения в сообщение данным эмулятором
###### Функции: ######  
- служит для проверки на корректность исходного сообщения (Check Module)
- служит для сохранения данных в БД (Services Module)
##### Parsed Field Message #####
###### Что из себя представляет: ######
Поле сообщения с параметрами, определёнными спецификацией ПВУ.
###### Функции: ######  
- представление данных, заключённых в отдельном поле сообщения, а также возможность работать с ним согласно спецификации ПВУ
- позволяет проверить на корректность данные, записанные в конкретном поле сообщения (Check Module)
- позволяет вычленить данные для сохранения в БД (Services Module)
- позволяет вычленить данные, необходимые для формирования ответа на запрос [0110] (Formation Module)
##### Parsed Element Model #####
###### Что из себя представляет: ######
Элемент, с параметрами, определёнными спецификацией ПВУ.
###### Функции: ###### 
- представление данных, заключённых в отдельном элементе поля, а также возможность работать с ним согласно спецификации ПВУ
- позволяет проверить на корректность данные, записанные в конкретном элементе поля (Check Module)
- позволяет вычленить данные для сохранения в БД (Services Module)

Parse Module
------------
##### Функции: #####
- парсинг Encoded Message в Parsed Message
- шифрование Parsed Message в Encoded Message (на выходе String с JSON-представлением Encoded Message)
##### Связи с другими модулями: #####
- использует Lib Module (библиотека ISO-8583 Java Lib)
- вызывается из Routing Module

Formation Module *
----------------
##### Функции: #####
Формирование сообщения-ответа на сообщение-запрос на основе ParsedMessage. Формирование происходит по правилам ПВУ.
##### Связи с другими модулями: #####
- использует Models Module (ParsedMessage)
- вызываетя из Routing Module

Check Module *
----------------
##### Функции: #####
Проверка сообщения на корректность согласно спецификации ПВУ.
##### Связи с другими модулями: #####
- использует Models Module (ParsedMessage)
- вызываетя из Routing Module

"*" - модули будут добавлены в ближайшем будущем.

## ```Controllers```
Модуль, по которому сервисы могут получить данные из БД
> Для получения данных из БД, этот модуль обращается 
> к соответствующему "сервису" (интерфейсу) из модуля ```Services``` 

## ```Repositories```

Модуль взаимодействия с БД. Отвечает за **добавления**, **удаления**,
**извлечения** данных из БД.  
```IMessageRepository``` - интерфейс для взаимодействия с БД
для ```ParsedMessage``` объекта. Интерфейс содержит функции **добавления**, **удаления**, **извлечения** сообщений. 
А также можно **удалять** или **извлекать** сообщения по их 
определенным свойствам.
> Для каждого объекта, хранящегося в БД, будет создаваться свой интерфейс

## ```Services```
Модуль для хранения всей логики объекта, таких как:
- Проверка валидности объекта
- Изменения значений полей объекта
- Добавления объекта в БД (отличие от модуля ```Repositories``` см. ниже)
- и т.д.
> Отличие от ```Repositories```:  
> Модуль ```Repositories``` отвечает только за взаимодействие с БД,
> тогда как ```Services``` отвечает за операции выполняемые над объектом,
> то есть отвечает за логику объекта. 
> А для добавления объекта в БД, этот модуль обращается к ```Repositories```

> Для каждого объекта будет создаваться свой интерфейс, и его реализация,
> которая будет содержать всю логику объекта

## ```Routing```
Модуль отвечающий за взаимодействие нашей платформы с Эквайером и Эмитент,
то есть отвечает за перенаправления сообщений.
> **Проект запускается с этого модуля (класс ```Application```)**