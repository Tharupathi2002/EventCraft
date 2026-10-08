# Module: Budget & Expense Management
Owner: Weerasinghe N.A.W.S.D. (IT25102007)
Proposal section: 6.4

Suggested layout (same pattern as Sujeewan's `eventplanning` package):
- entity/      -> JPA entities (e.g. Budget.java, Expense.java)
- dto/         -> request/response DTOs
- repository/  -> Spring Data JPA repositories
- service/     -> interface + Impl business logic
- controller/  -> @RestController REST endpoints (suggested base path: /api/budgets)
- exception/   -> custom exceptions + @RestControllerAdvice

Delete this README once you add your own files.
