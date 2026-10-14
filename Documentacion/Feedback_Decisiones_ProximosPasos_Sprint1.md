# Sprint Review — Sprint 1
## Feedback, Decisiones y Próximos Pasos

---

## 1. Feedback Recibido

### Del Product Owner

| # | Comentario | Prioridad |
|---|---|---|
| 1 | "La autenticación funciona correctamente, pero me gustaría que el mensaje de error cuando la contraseña es incorrecta sea más específico, indicando si el usuario no existe o la contraseña es inválida." | Media |
| 2 | "El CRUD de pedidos está completo, pero necesito que al crear un pedido, el sistema valide que haya suficiente stock de materiales antes de permitir guardarlo." | Alta |
| 3 | "El CRUD de inventario está bien, pero sería útil poder buscar materiales por nombre o categoría, no solo ver la lista completa." | Media |
| 4 | "El calendario de pedidos es visualmente claro, pero necesito que se pueda hacer clic en un pedido del calendario y que me lleve directamente a la vista de detalle o edición." | Alta |
| 5 | "El cierre de sesión funciona, pero me gustaría que apareciera un mensaje de confirmación antes de cerrar, para evitar cierres accidentales." | Baja |

### Del Scrum Master (auto-observación)

| # | Comentario |
|---|---|
| 1 | "El tiempo de carga de la lista de pedidos es aceptable, pero podría optimizarse la consulta a la base de datos." |
| 2 | "La validación de datos en los formularios funciona, pero faltan validaciones del lado del servidor (no solo del cliente)." |
| 3 | "El código está bien estructurado, pero falta documentación en los métodos principales." |

---

## 2. Decisiones Tomadas

### Sobre el Product Backlog

| Decisión | Detalle |
|---|---|
| **HU-12 se adelanta al Sprint 2** | La validación de stock al crear pedidos (feedback #2 del PO) se considera crítica. Se decide incluirla en el Sprint 2 como parte de las reglas de negocio. |
| **Búsqueda en inventario se posterga** | La funcionalidad de búsqueda de materiales (feedback #3) se mueve al Sprint 3, ya que no es bloqueante para el flujo principal. |
| **Click en calendario se mantiene** | La navegación desde el calendario al detalle del pedido (feedback #4) se confirma para el Sprint 2 como mejora de UX. |
| **Confirmación de cierre de sesión se descarta** | El mensaje de confirmación (feedback #5) se considera innecesario para el flujo actual. No se implementa. |

### Sobre el proceso

| Decisión | Detalle |
|---|---|
| **Refinamiento de historias** | Antes del Sprint Planning 2, el Product Owner refinará las historias HU-04, HU-05, HU-09 y HU-13 con criterios de aceptación más detallados. |
| **Definición de "Done"** | Se acuerda que una historia está "Done" solo cuando: (1) el código está en el repositorio, (2) pasó pruebas QA, (3) el PO validó la funcionalidad. |
| **Daily Scrum** | Se mantienen las reuniones diarias de 15 minutos para seguimiento del Sprint 2. |

---

## 3. Próximos Pasos

### Para el Sprint 2

| Actividad | Responsable | Fecha estimada |
|---|---|---|
| Sprint Planning 2 | Scrum Master + Product Owner | Día 1 del Sprint 2 |
| Refinimiento de historias HU-04, HU-05, HU-09, HU-13 | Product Owner | Antes del Sprint Planning 2 |
| Implementación de HU-04: Notificaciones pedidos | Scrum Master | Sprint 2 |
| Implementación de HU-05: Chat BOT | Scrum Master | Sprint 2 |
| Implementación de HU-09: Función rápida agregar material | Scrum Master | Sprint 2 |
| Implementación de HU-13: Vista Prioritaria de Pedidos | Scrum Master | Sprint 2 |
| Validación de stock al crear pedido (regla de negocio) | Scrum Master | Sprint 2 |
| Navegación desde calendario a detalle de pedido | Scrum Master | Sprint 2 |
| Pruebas QA | Scrum Master | Últimos 2 días del Sprint 2 |
| Sprint Review 2 | Scrum Master + Product Owner | Último día del Sprint 2 |
| Sprint Retrospective 2 | Scrum Master | Último día del Sprint 2 |

### Pendientes para el Sprint 3

| Historia | Puntos | Nota |
|---|---|---|
| HU-11: Copia de respaldo automática | 12 | Se mantiene para Sprint 3 |
| HU-12: Reducción de materiales al completar pedido | 10 | Se mantiene para Sprint 3 |
| HU-07: Dashboard-Costos y ganancias | 6 | Sprint 3 |
| HU-08: DashBoard-EstadoPedidos | 6 | Sprint 3 |
| HU-14: Predicción de materiales | 22 | Sprint 3 |
| Búsqueda de materiales en inventario | — | Mejora postergada del Sprint 1 |

---

## 4. Métricas del Sprint 1

| Métrica | Valor |
|---|---|
| Puntos planificados | 30 |
| Puntos completados | 30 |
| Tasa de éxito | 100% |
| Velocidad del equipo | 30 puntos/sprint |
| Días del sprint | 5 |

---

## 5. Lecciones Aprendidas (para considerar en el Sprint 2)

| Lección | Acción |
|---|---|
| Las validaciones del lado del servidor son necesarias | Implementar validaciones en el backend para el Sprint 2 |
| El feedback del PO sobre reglas de negocio es crítico | Incluir validación de stock desde el inicio del Sprint 2 |
| La navegación entre vistas mejora la UX | Priorizar la integración calendario → detalle de pedido |
| El equipo reducido requiere planificación realista | Ajustar el compromiso del Sprint 2 a ~30 puntos |
