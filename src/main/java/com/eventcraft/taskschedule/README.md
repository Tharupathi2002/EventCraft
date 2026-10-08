# Module: Task & Schedule Management
Owner: Senanayake Y.I.D.P. (IT25104083)
Proposal section: 6.5

Suggested layout (same pattern as Sujeewan's `eventplanning` package):
- entity/      -> JPA entities (e.g. Task.java, ScheduleItem.java)
- dto/         -> request/response DTOs
- repository/  -> Spring Data JPA repositories
- service/     -> interface + Impl business logic
- controller/  -> @RestController REST endpoints (suggested base path: /api/tasks)
- exception/   -> custom exceptions + @RestControllerAdvice

Delete this README once you add your own files.
