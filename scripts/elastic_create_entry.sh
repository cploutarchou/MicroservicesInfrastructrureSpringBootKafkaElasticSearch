curl --location --request PUT 'localhost:9200/twitter-index/_doc/1' \
--header 'Content-Type: application/json' \
--data-raw '{
  "userId": 1,
  "id": 1,
  "createdAt": "2021-09-21T23:00:50+000",
  "text": "Some text"
}'