# Sprint Planning — Sprint 2

## Información General

| Campo | Valor |
|---|---|
| **Proyecto** | MANANI |
| **Sprint** | Sprint 2 |
| **Fecha** | [COMPLETAR] |
| **Duración** | 5 días |
| **Equipo** | 1 miembro (Scrum Master / Desarrollador) |
| **Product Owner** | [COMPLETAR] |
| **Scrum Master** | [COMPLETAR] |

---

## Participantes y Roles

| Rol | Nombre | Responsabilidad en la reunión |
|---|---|---|
| **Product Owner** | [COMPLETAR] | Presentar el objetivo del sprint, priorizar historias, definir criterios de aceptación |
| **Scrum Master** | [COMPLETAR] | Facilitar la reunión, estimar esfuerzo, comprometer el trabajo, crear el sprint backlog |

---

## Guion del Product Owner

### Apertura (5 min)

> "Bienvenidos al Sprint Planning del Sprint 2. El objetivo de hoy es planificar qué vamos a entregar en los próximos 5 días. Como Product Owner, mi responsabilidad es asegurarnos de que estamos trabajando en las funcionalidades que más valor aportan al producto."

### Presentación del Objetivo del Sprint (5 min)

> "El objetivo del Sprint 2 es: **Implementar el módulo de automatizaciones, reglas de negocio y notificaciones.**"

Esto incluye:
- Notificaciones de pedidos (envío por correo)
- Chat BOT para consultas de disponibilidad y reglas de negocio
- Función rápida de agregar material
- Vista prioritaria de pedidos próximos a vencer
- Validación de stock al crear pedido (regla de negocio)
- Navegación desde calendario a detalle de pedido

### Presentación de las Historias (15 min)

> "Las historias que propongo para este sprint, en orden de prioridad, son:"

| Prioridad | ID | Historia | Puntos | Justificación |
|---|---|---|---|---|
| 1 | HU-04 | Notificaciones pedidos | 6 | El PO marcó como prioridad alta tener notificaciones automáticas |
| 2 | HU-05 | Chat BOT | 15 | Funcionalidad clave para la experiencia del usuario |
| 3 | HU-09 | Función rápida agregar material | 4 | Mejora de productividad, esfuerzo bajo |
| 4 | HU-13 | Vista Prioritaria de Pedidos Próximos a Vencer | 5 | Ayuda a priorizar trabajo, esfuerzo medio |
| — | HU-12 | Validación de stock al crear pedido | — | Regla de negocio crítica identificada en Sprint Review 1 |
| — | Mejora | Navegación calendario → detalle | — | Mejora de UX identificada en Sprint Review 1 |

> "En total, son 30 puntos de historia, lo cual es consistente con la velocidad que logramos en el Sprint 1."

### Criterios de Aceptación (15 min)

> "Para cada historia, estos son los criterios que deben cumplirse para considerarla terminada:"

#### HU-04: Notificaciones pedidos
- [ ] Al crear un pedido, se envía automáticamente un correo de notificación
- [ ] El correo incluye: número de pedido, cliente, fecha de entrega y materiales
- [ ] Existe un menú de ajustes para configurar las notificaciones
- [ ] Se puede activar/desactivar las notificaciones por correo
- [ ] El sistema maneja errores de envío (sin conexión, correo inválido)

#### HU-05: Chat BOT
- [ ] El chat es accesible desde la interfaz principal
- [ ] El bot responde consultas sobre disponibilidad de materiales
- [ ] El bot responde consultas sobre reglas de negocio (tiempos de entrega, estados)
- [ ] El bot está integrado con el sistema de notificaciones
- [ ] El bot maneja consultas no reconocidas con un mensaje amigable

#### HU-09: Función rápida agregar material
- [ ] Existe un botón de "agregado rápido" en la vista de inventario
- [ ] Al hacer clic, se abre un formulario simplificado
- [ ] El formulario permite: nombre, cantidad, unidad de medida
- [ ] El material se guarda correctamente en la base de datos
- [ ] El material aparece en la lista de inventario inmediatamente

#### HU-13: Vista Prioritaria de Pedidos Próximos a Vencer
- [ ] Existe un filtro para mostrar solo pedidos próximos a vencer
- [ ] Los pedidos se resaltan visualmente (color de advertencia)
- [ ] Los pedidos se ordenan por fecha de vencimiento
- [ ] Se puede configurar el umbral de días para considerar "próximo a vencer"
- [ ] La vista es responsive y accesible

#### HU-12: Validación de stock al crear pedido
- [ ] Al crear un pedido, el sistema verifica que haya suficiente stock
- [ ] Si no hay stock, muestra un error claro indicando qué material falta
- [ ] El pedido no se guarda si no hay stock suficiente
- [ ] El sistema muestra la cantidad disponible vs la requerida

#### Mejora: Navegación calendario → detalle
- [ ] Se puede hacer clic en un pedido del calendario
- [ ] Al hacer clic, se redirige a la vista de detalle del pedido
- [ ] La vista de detalle muestra toda la información del pedido
- [ ] Se puede editar el pedido desde esta vista

### Cierre del Product Owner (5 min)

> "Estas son las historias que propongo para el Sprint 2. ¿Hay alguna pregunta o ajuste antes de que el Scrum Master proceda con la estimación y el compromiso?"

---

## Guion del Scrum Master

### Apertura (3 min)

> "Gracias [Nombre del PO]. Como Scrum Master, mi rol en esta reunión es facilitar la planificación y asegurarnos de que el trabajo comprometido sea realista y alcanzable en los 5 días del sprint."

### Revisión de la Velocidad del Sprint 1 (3 min)

> "En el Sprint 1 completamos 32 puntos de historia en 5 días. Para este Sprint 2, el PO propone 30 puntos, lo cual es consistente con nuestra velocidad."

| Métrica | Sprint 1 | Sprint 2 (propuesto) |
|---|---|---|
| Puntos planificados | 32 | 30 |
| Puntos completados | 32 | — |
| Velocidad | 32 pts/sprint | 30 pts/sprint |
| Días | 5 | 5 |

### Desglose de Tareas (15 min)

> "Voy a desglosar cada historia en tareas técnicas para asegurar que el trabajo esté bien planificado:"

#### HU-04: Notificaciones pedidos (6 pts)
| # | Tarea | Estimación |
|---|---|---|
| 1 | Configurar servicio de envío de correos | 1.0 día |
| 2 | Crear plantilla de correo de notificación | 0.5 día |
| 3 | Integrar envío de correo al crear pedido | 0.5 día |
| 4 | Crear menú de ajustes de notificaciones | 0.5 día |
| 5 | Pruebas y ajustes | 0.5 día |

#### HU-05: Chat BOT (15 pts)
| # | Tarea | Estimación |
|---|---|---|
| 1 | Definir reglas de negocio del bot | 1.0 día |
| 2 | Implementar lógica de respuestas | 2.0 días |
| 3 | Crear interfaz del chat | 1.0 día |
| 4 | Integrar con notificaciones | 0.5 día |
| 5 | Pruebas y ajustes | 0.5 día |

#### HU-09: Función rápida agregar material (4 pts)
| # | Tarea | Estimación |
|---|---|---|
| 1 | Crear formulario simplificado | 0.5 día |
| 2 | Implementar guardado rápido | 0.5 día |
| 3 | Integrar con la vista de inventario | 0.5 día |
| 4 | Pruebas y ajustes | 0.5 día |

#### HU-13: Vista Prioritaria de Pedidos (5 pts)
| # | Tarea | Estimación |
|---|---|---|
| 1 | Implementar lógica de filtrado por fecha | 0.5 día |
| 2 | Crear resaltado visual de pedidos próximos | 0.5 día |
| 3 | Configurar umbral de días | 0.5 día |
| 4 | Pruebas y ajustes | 0.5 día |

#### HU-12: Validación de stock (regla de negocio)
| # | Tarea | Estimación |
|---|---|---|
| 1 | Implementar verificación de stock al crear pedido | 1.0 día |
| 2 | Crear mensajes de error claros | 0.5 día |
| 3 | Pruebas y ajustes | 0.5 día |

#### Mejora: Navegación calendario → detalle
| # | Tarea | Estimación |
|---|---|---|
| 1 | Implementar click en pedido del calendario | 0.5 día |
| 2 | Redirigir a vista de detalle | 0.5 día |
| 3 | Pruebas y ajustes | 0.5 día |

### Compromiso del Sprint (5 min)

> "Con base en el desglose de tareas, me comprometo a entregar las siguientes historias en el Sprint 2:"

| ID | Historia | Puntos | Estado |
|---|---|---|---|
| HU-04 | Notificaciones pedidos | 6 | Comprometida |
| HU-05 | Chat BOT | 15 | Comprometida |
| HU-09 | Función rápida agregar material | 4 | Comprometida |
| HU-13 | Vista Prioritaria de Pedidos Próximos a Vencer | 5 | Comprometida |
| HU-12 | Validación de stock al crear pedido | — | Comprometida (regla de negocio) |
| Mejora | Navegación calendario → detalle | — | Comprometida (mejora UX) |
| **Total** | | **30** | |

### Plan de Trabajo Día a Día (5 min)

> "Este es el plan de trabajo propuesto para los 5 días del sprint:"

| Día | Actividad principal |
|---|---|
| **Día 1** | Sprint Planning + Configuración de servicio de correos + Inicio HU-04 |
| **Día 2** | Completar HU-04 + Inicio HU-05 (reglas de negocio del bot) |
| **Día 3** | Completar HU-05 + Inicio HU-09 |
| **Día 4** | Completar HU-09 + Inicio HU-13 + HU-12 (validación de stock) |
| **Día 5** | Completar HU-13 + HU-12 + Mejora navegación + Pruebas QA + Sprint Review |

### Cierre del Scrum Master (3 min)

> "El plan está listo. El Sprint Backlog queda comprometido con 30 puntos de historia. Nos vemos en la Daily Scrum de mañana para comenzar."

---

## Acuerdos del Sprint Planning

| Acuerdo | Detalle |
|---|---|
| **Objetivo del Sprint** | Implementar el módulo de automatizaciones, reglas de negocio y notificaciones |
| **Historias comprometidas** | HU-04, HU-05, HU-09, HU-13, HU-12 (regla de negocio), Mejora navegación |
| **Puntos totales** | 30 puntos de historia |
| **Duración** | 5 días |
| **Definición de "Done"** | Código en repo + pruebas QA + validación PO |
| **Daily Scrum** | 15 minutos diarios |
| **Sprint Review** | Último día del sprint |
| **Sprint Retrospective** | Último día del sprint (después del Review) |

---

## Riesgos Identificados

| Riesgo | Probabilidad | Impacto | Mitigación |
|---|---|---|---|
| El Chat BOT es la historia más grande (15 pts) y puede tomar más tiempo | Media | Alto | Priorizarla desde el Día 2, dedicar la mayor parte del tiempo |
| El servicio de correos puede tener problemas de configuración | Baja | Medio | Configurar y probar el servicio el Día 1 |
| El equipo reducido (1 persona) limita la capacidad | Alto | Alto | Mantener el compromiso en 30 pts, no agregar más trabajo |
| Dependencia entre HU-04 y HU-05 (bot integra notificaciones) | Media | Medio | Completar HU-04 antes de finalizar HU-05 |

---

## Sprint Backlog — Sprint 2

| # | Tarea | Historia | Estimación | Estado |
|---|---|---|---|---|
| 1 | Configurar servicio de envío de correos | HU-04 | 1.0 día | Sin iniciar |
| 2 | Crear plantilla de correo de notificación | HU-04 | 0.5 día | Sin iniciar |
| 3 | Integrar envío de correo al crear pedido | HU-04 | 0.5 día | Sin iniciar |
| 4 | Crear menú de ajustes de notificaciones | HU-04 | 0.5 día | Sin iniciar |
| 5 | Pruebas y ajustes HU-04 | HU-04 | 0.5 día | Sin iniciar |
| 6 | Definir reglas de negocio del bot | HU-05 | 1.0 día | Sin iniciar |
| 7 | Implementar lógica de respuestas | HU-05 | 2.0 días | Sin iniciar |
| 8 | Crear interfaz del chat | HU-05 | 1.0 día | Sin iniciar |
| 9 | Integrar con notificaciones | HU-05 | 0.5 día | Sin iniciar |
| 10 | Pruebas y ajustes HU-05 | HU-05 | 0.5 día | Sin iniciar |
| 11 | Crear formulario simplificado | HU-09 | 0.5 día | Sin iniciar |
| 12 | Implementar guardado rápido | HU-09 | 0.5 día | Sin iniciar |
| 13 | Integrar con la vista de inventario | HU-09 | 0.5 día | Sin iniciar |
| 14 | Pruebas y ajustes HU-09 | HU-09 | 0.5 día | Sin iniciar |
| 15 | Implementar lógica de filtrado por fecha | HU-13 | 0.5 día | Sin iniciar |
| 16 | Crear resaltado visual de pedidos próximos | HU-13 | 0.5 día | Sin iniciar |
| 17 | Configurar umbral de días | HU-13 | 0.5 día | Sin iniciar |
| 18 | Pruebas y ajustes HU-13 | HU-13 | 0.5 día | Sin iniciar |
| 19 | Implementar verificación de stock al crear pedido | HU-12 | 1.0 día | Sin iniciar |
| 20 | Crear mensajes de error claros | HU-12 | 0.5 día | Sin iniciar |
| 21 | Pruebas y ajustes HU-12 | HU-12 | 0.5 día | Sin iniciar |
| 22 | Implementar click en pedido del calendario | Mejora | 0.5 día | Sin iniciar |
| 23 | Redirigir a vista de detalle | Mejora | 0.5 día | Sin iniciar |
| 24 | Pruebas y ajustes Mejora | Mejora | 0.5 día | Sin iniciar |
| 25 | Pruebas QA finales | — | 1.0 día | Sin iniciar |
| 26 | Sprint Review | — | — | Sin iniciar |
| 27 | Sprint Retrospective | — | — | Sin iniciar |

---

## Notas Adicionales

- [COMPLETAR] Cualquier nota o acuerdo adicional de la reunión
