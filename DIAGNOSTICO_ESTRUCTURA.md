# Diagnóstico de estructura del repositorio (GestorGastos)

## Resumen ejecutivo
La base del proyecto está **bien encaminada para un gestor de gastos diarios**: ya tiene separación por capas (UI, repositorio, base local), navegación por pantallas, categorías, reportes, exportación CSV y bloqueo por PIN.

A nivel estructural, el estado actual es de un **MVP funcional** con oportunidades claras para escalar mantenibilidad, pruebas y evolución de funcionalidades.

## Lo que está bien resuelto

### 1) Arquitectura por capas (simple y entendible)
- Se observa un flujo `UI (Compose) -> ViewModel -> Repository -> DAO (Room)`.
- Las entidades y DAOs están organizadas por dominio en `data/local`.
- Hay un contenedor de dependencias básico (`AppContainer`) que centraliza repositorios y seguridad.

**Impacto:** facilita incorporar nuevas pantallas o consultas sin romper toda la app.

### 2) Persistencia local alineada al caso de uso
- `Room` con entidades de gastos y categorías.
- `ExpenseEntity` guarda `amountMinor` (minor units), muy buena decisión para evitar errores de coma flotante en montos.
- Índices en `dateEpochDay` y `categoryId`, útiles para filtros mensuales y reportes por categoría.

**Impacto:** buena base para consultas de historial y análisis.

### 3) Funcionalidades núcleo ya presentes
- Alta/edición/listado de gastos.
- Categorías administrables.
- Reportes por mes y por moneda.
- Exportación CSV.
- Adjuntos de recibos con `FileProvider`.
- PIN local con llave en Android Keystore (HMAC).

**Impacto:** ya cubre gran parte de un producto usable para seguimiento diario.

## Hallazgos estructurales a mejorar (priorizados)

### Alta prioridad
1. **Falta una capa de dominio explícita (use cases/interactors).**
   - Hoy la lógica de negocio está repartida entre ViewModels y utilidades.
   - Para crecer (presupuestos, metas, alertas, sincronización), conviene encapsular reglas de negocio en casos de uso.

2. **Escasa estrategia de pruebas.**
   - Solo existen tests de ejemplo generados por plantilla.
   - No hay tests unitarios de parseo de montos, validación de formularios, ni tests de repositorio/DAO.

3. **Control de errores limitado.**
   - Varios flujos retornan `Boolean` o asumen éxito.
   - Falta modelar estados con `Result`/`sealed class` (`Loading`, `Success`, `Error`) y mensajes de error consistentes en UI.

### Media prioridad
4. **Inyección de dependencias manual y centralizada en `AppContainer`.**
   - Funciona para MVP, pero al crecer aumenta el acoplamiento.
   - Migrar a Hilt/Koin simplificaría factories, scopes y testeo.

5. **Navegación muy cargada en `AppNav`.**
   - `AppNav` concentra orquestación, armado de VMs y lógica auxiliar (CSV), lo que dificulta su mantenibilidad.
   - Conviene extraer builders/handlers por feature o módulos de navegación.

6. **Dependencias duplicadas o mezcladas en Gradle.**
   - Se usan aliases del catalog y además coordenadas literales para algunas librerías de lifecycle/navigation.
   - Unificar en `libs.versions.toml` mejora consistencia.

### Baja prioridad
7. **Convenciones de estilo y estructura con pequeñas inconsistencias.**
   - Ejemplo: nombre de archivo `AppDataBase.kt` vs clase `AppDatabase`.
   - Espaciados/imports y comentarios mezclan estilos.

8. **Documentación técnica mínima.**
   - README describe qué hace la app, pero no arquitectura, decisiones técnicas ni roadmap.

## Diagnóstico de ajuste al objetivo “gestor de gastos diarios”

**Conclusión:** el repositorio está en una fase correcta para el objetivo.
- Para uso diario individual/offline, la estructura actual es suficiente.
- Para evolucionar a un producto más robusto (analítica avanzada, sincronización, multiusuario, respaldo seguro), será clave reforzar arquitectura y calidad (dominio + pruebas + errores + DI).

## Hoja de ruta sugerida (4 iteraciones)

### Iteración 1 (calidad base)
- Añadir tests unitarios de:
  - parseo de montos,
  - validaciones de formularios,
  - mapeos DAO/repository.
- Estandarizar manejo de errores con un modelo de estado para pantalla.

### Iteración 2 (arquitectura)
- Introducir casos de uso (ej. `CreateExpense`, `UpdateExpense`, `GetMonthlyReport`).
- Reducir lógica de negocio dentro de ViewModels.

### Iteración 3 (mantenibilidad)
- Migrar DI manual a Hilt.
- Dividir navegación/feature boundaries para reducir tamaño de `AppNav`.

### Iteración 4 (producto)
- Presupuestos por categoría/mes.
- Objetivos de ahorro.
- Recordatorios/alertas.
- Copia de seguridad/restauración y eventualmente sincronización.

---
Si quieres, en el siguiente paso te puedo proponer una **arquitectura objetivo concreta** (paquetes, contratos, casos de uso y plan de migración incremental) sin romper lo que ya tienes funcionando.
