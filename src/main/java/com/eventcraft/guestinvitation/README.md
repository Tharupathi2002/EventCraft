# Module: Guest & Invitation Management
Owner: Wijesiri M.H.O.T. (IT25101102)
Proposal section: 6.2

Suggested layout (same pattern as Sujeewan's `eventplanning` package):
- entity/      -> JPA entities (e.g. Guest.java, RsvpStatus.java)
- dto/         -> request/response DTOs
- repository/  -> Spring Data JPA repositories
- service/     -> interface + Impl business logic
- controller/  -> @RestController REST endpoints (suggested base path: /api/guests)
- exception/   -> custom exceptions + @RestControllerAdvice

Delete this README once you add your own files.
