# Clínica de turnos prioritarios

Proyecto del **equipo 8** para el segundo examen parcial de Estructuras de Datos (curso 2026–2027). El objetivo es modelar la espera de pacientes por especialidad y aplicar prioridad de emergencias, expiración de turnos regulares, historial de cancelaciones y conteo recursivo.

## Estado del proyecto

La lógica clínica está implementada: registro, atención por prioridad, cancelación manual, expiración y conteo recursivo. `Main` ejecuta una demostración con un reloj simulado; `PruebaClinica` comprueba los flujos funcionales y los casos negativos. El programa trabaja en memoria: al finalizar la ejecución se pierden sus datos. La presentación oral sigue pendiente.

## Reglas del equipo 8

- **Multilista:** `Clínica → especialidades o áreas → pacientes en espera`. Cada paciente tiene ID único, nombre, tipo (`REGULAR` o `EMERGENCIA`) y fecha/hora de registro. Se usa `LocalDateTime` para conservar hora y minuto y calcular esperas que crucen la medianoche.
- **Cola dual:** cada especialidad tendrá una cola de emergencias y otra de regulares. Siempre se atenderá primero a una emergencia. Dentro de cada cola se conservará el orden de llegada (FIFO).
- **Expiración:** un paciente regular que lleve **más de 30 minutos** sin atención será retirado de la espera y registrado como abandonado por tiempo excedido. A los 30 minutos exactos todavía no expira. La regla no se aplica a emergencias.
- **Pila:** solo las cancelaciones manuales de turnos se guardarán en orden LIFO. Una cancelación manual no aumentará el conteo de abandonos por tiempo.
- **Recursividad:** un método recorrerá los nodos externos de las especialidades y sumará sus abandonos, con `null` como caso base y sin `for` ni `while` en ese cálculo.

La sublista de la multilista y las dos colas contienen referencias a los mismos objetos `Paciente`. Las operaciones de `Clinica` actualizan ambas estructuras al registrar o retirar un turno. Para cancelar y descartar se utilizan colas auxiliares mediante `add` y `poll`, conservando FIFO sin agregar operaciones ajenas a la cola de la conferencia. Las estructuras usan nodos propios, sin `java.util.Queue`, `Stack`, `ArrayList` ni `LinkedList`.

Cada especialidad conserva un historial enlazado de pacientes abandonados por tiempo (el más reciente queda primero), además de su contador. El total se calcula recursivamente sobre los nodos externos, sumando los contadores de las especialidades. El historial de cancelaciones manuales permanece en `LinkedStack<Cancelacion>`.

## Usar la lógica clínica

- `agregarEspecialidad(nombre)` crea una especialidad; rechaza nombres vacíos o repetidos.
- `registrarPaciente(especialidad, paciente)` valida los datos, rechaza un ID que ya tenga un turno en espera en cualquier especialidad y registra el turno en ambas estructuras. Un paciente puede volver a registrarse después de salir de la espera.
- `atenderSiguiente(especialidad, ahora)` procesa las expiraciones y devuelve la primera emergencia o, si no hay ninguna, el primer regular. Devuelve `null` cuando no queda nadie en espera.
- `cancelarTurno(id, ahora)` retira el turno y lo apila. Si el regular ya superó los 30 minutos, se registra como abandono y se rechaza la cancelación manual.
- `expirarRegulares(ahora)` permite avanzar el reloj de la simulación: revisa todos los regulares, incluso detrás del frente, retira los vencidos y registra cada abandono una sola vez.
- `totalAbandonosRecursivo()` devuelve el total acumulado; una clínica vacía devuelve cero.

La expiración se comprueba automáticamente al registrar (usando la hora de registro), atender y completar una cancelación. También puede ejecutarse explícitamente al avanzar el tiempo. No hay un temporizador en segundo plano: todas las horas son parámetros para poder demostrar y probar los límites sin esperar 30 minutos reales. Una atención o cancelación anterior al registro del paciente se rechaza.

Los datos obligatorios inválidos, las especialidades inexistentes, los turnos inexistentes y los duplicados producen `IllegalArgumentException` con un mensaje explicativo. Los nombres e IDs se comparan distinguiendo mayúsculas, después de quitar espacios en los extremos. Los getters permiten inspeccionar nodos e historiales; las altas y bajas de pacientes deben hacerse mediante `Clinica` para mantener la sincronización.

## Clases

| Clase | Responsabilidad |
| --- | --- |
| `Node<E>` | Nodo genérico `info`/`next` usado por pila y colas, como en las conferencias 3 y 4. |
| `LinkedQueue<E>` | Cola enlazada con `first`/`last` y operaciones `add`, `poll`, `peek`, `isEmpty`. |
| `LinkedStack<E>` | Pila enlazada con `top` y operaciones `push`, `pop`, `top`, `isEmpty`. |
| `NodoExterno` | Nodo de especialidad con referencia a su sublista y al siguiente nodo externo. |
| `NodoInterno` | Nodo de paciente con referencia al siguiente paciente de su especialidad. |
| `Multilista` | Cabeza de especialidades e inserción, búsqueda y eliminación enlazada de pacientes, según la conferencia 5. |
| `Paciente` | Datos del paciente y tipo de turno. |
| `Especialidad` | Nombre, dos colas de espera, historial enlazado y número de abandonos por tiempo. |
| `Cancelacion` | Paciente, especialidad y momento de una cancelación manual. |
| `Clinica` | Coordina las estructuras y realiza las operaciones clínicas y el conteo recursivo. |
| `Main` | Demostración de atención prioritaria, cancelación y abandono por tiempo. |
| `PruebaEstructuras` | Comprobaciones básicas de pila, cola y multilista. |
| `PruebaClinica` | Pruebas funcionales, negativas y de borde sin dependencias externas. |

## Abrir y comprobar en IntelliJ IDEA

Abrir la carpeta raíz como proyecto Java y marcar `src/` como **Sources Root** si IntelliJ no lo detecta. Seleccionar un JDK 17 o posterior y ejecutar `Main`, `PruebaClinica` o `PruebaEstructuras`. Para esta última deben habilitarse las aserciones (`-ea`). No se necesita Maven ni Gradle.

En PowerShell también puede compilarse y ejecutarse así:

```powershell
New-Item -ItemType Directory -Force out | Out-Null
javac -encoding UTF-8 --release 17 -d out (Get-ChildItem src\*.java).FullName
java -ea -cp out PruebaEstructuras
java -ea -cp out PruebaClinica
java -cp out Main
```

`out/` contiene archivos generados y está excluida de Git.

## Casos de prueba ejecutados

| Caso | Resultado esperado |
| --- | --- |
| Dos emergencias y dos regulares intercalados | Primero salen las emergencias; dentro de cada tipo se respeta FIFO. |
| Regular con 30 minutos de espera | Continúa en cola. |
| Regular con 31 minutos de espera | Se retira y aumenta en uno el abandono de su especialidad. |
| Emergencia con más de 30 minutos | No expira por esta regla. |
| Cancelación manual | Se retira de la espera, aparece en la cima de la pila y no cuenta como abandono. |
| Clínica o colas vacías | Se informa que no hay paciente para atender, sin fallar. |
| Especialidad o paciente inexistente, ID duplicado | Se rechaza la operación y se conserva el turno válido. |
| Varias especialidades | El conteo recursivo devuelve la suma de sus abandonos. |

`PruebaClinica` ejecuta los casos de la tabla y comprueba también la sincronización entre sublistas y colas, la conservación de FIFO al filtrar, el cruce de medianoche, la expiración de pacientes detrás del frente, el rechazo de cancelaciones de turnos vencidos, las fechas anteriores al registro, los campos obligatorios y el registro individual de los abandonos. Se ejecutaron junto con `PruebaEstructuras`, con resultado correcto. `Main` muestra una emergencia atendida primero, una cancelación manual y dos abandonos en distintas especialidades; el total recursivo es 2.

## Entrega pendiente

La orden exige un proyecto Java funcional con pruebas positivas, negativas y de borde; un README documentado; una presentación en PDF o diapositivas incluida o enlazada; y una exposición oral de 18–20 minutos. Queda preparar y añadir la presentación, además de ensayar la explicación de las estructuras y sus algoritmos.

## Declaración de uso de inteligencia artificial

Se utilizó Codex para interpretar la orden y las conferencias, proponer el diseño de clases, implementar la lógica clínica y las estructuras enlazadas, redactar este README y preparar y ejecutar las pruebas básicas y funcionales. La presentación oral sigue pendiente. Los integrantes deben revisar y comprender el código y actualizar esta declaración si emplean IA en otras partes del trabajo.
