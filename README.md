# gRPC Key-Value Service 

Простой gRPC сервис key-value хранилища на Java с использованием Tarantool.

## Функциональность

Сервис поддерживает операции:

- `Put` — сохранить значение по ключу
- `Get` — получить значение по ключу
- `Delete` — удалить запись
- `Count` — количество записей
- `Range` — получить диапазон ключей (streaming)

Особенности:
- поддержка `null` значений
- streaming для range
- Tarantool в качестве хранилища
- gRPC API

---

## Запуск Tarantool

```bash
docker compose up -d
```

## Проверка через grpcurl

### Список сервисов

```bash
grpcurl -plaintext localhost:9090 list
```

---

###  Put

```bash
grpcurl -plaintext -d '{
  "key": "k1",
  "value": "aGVsbG8=",
  "has_value": true
}' localhost:9090 KvService/Put
```

---

###  Put (null)

```bash
grpcurl -plaintext -d '{
  "key": "k2",
  "has_value": false
}' localhost:9090 KvService/Put
```

---

###  Get

```bash
grpcurl -plaintext -d '{
  "key": "k1"
}' localhost:9090 KvService/Get
```

---

###  Count

```bash
grpcurl -plaintext -d '{}' localhost:9090 KvService/Count
```

---

###  Delete

```bash
grpcurl -plaintext -d '{
  "key": "k1"
}' localhost:9090 KvService/Delete
```

---

###  Range (streaming, с limit)

```bash
grpcurl -plaintext -d '{
  "key_since": "a",
  "key_to": "z",
  "limit": 1000
}' localhost:9090 KvService/Range
```

---

## Тестирование масштабируемости

### Встроенный бенчмарк

```bash
./gradlew build
java -cp build/libs/grpc-service-java-1.0-SNAPSHOT.jar ru.cu.advancedgit.Main --benchmark
```

**Проверяет:**
- Вставка 100,000 записей
- Count на большом наборе
- Range запросы с limit
-  1,000 Get операций
-  1,000 Delete операций

**Оптимизация для масштабируемости:**
- Вторичный индекс `key_range` для быстрого скана
- Lua запросы используют индекс вместо полного скана пространства

---

## ️ Лимиты и ограничения

### Range запрос
- **Max результат: 100,000 записей** за одну операцию
- `limit=0` или `limit > 100,000` → автоматически ограничивается до 100k
- Streaming предотвращает загрузку всех данных в память одновременно