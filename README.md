# Hero configuration API

Hero configuration API created with Scala and Play Framework.

## Endpoints 

| Endpoint  | HTTP Method | Body                           | Query Parameters for filtering                                           |
|-----------|-------------|--------------------------------|--------------------------------------------------------------------------|
| /hero     | GET         |                                | `name (string)`, `faction (string)`, `minAttack (int)`, `maxAttack (int)`|
| /hero     | POST        | `model.Hero` without `_id`     |                                                                          |
| /hero/:id | DELETE      |                                |                                                                          |
| /hero/:id | PUT         | `model.Hero` without `_id`     |                                                                          |


## Development

Start MongoDb with `docker compose up mongo` and the application with `sbt run`. The whole application can be run with docker compose: `docker compose up`

