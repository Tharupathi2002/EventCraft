# Module: Event Dashboards & Reports
Owner: Fernando S. D. Y. D. (IT25104010)
Proposal section: 6.6

Suggested layout (same pattern as Sujeewan's `eventplanning` package):
- entity/      -> JPA entities (e.g. Report.java)
- dto/         -> request/response DTOs
- repository/  -> Spring Data JPA repositories
- service/     -> interface + Impl business logic
- controller/  -> @RestController REST endpoints (suggested base path: /api/reports)
- exception/   -> custom exceptions + @RestControllerAdvice

Delete this README once you add your own files.
