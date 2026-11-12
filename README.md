# Clínica de turnos prioritarios

Proyecto del **equipo 8** para el segundo examen parcial de Estructuras de Datos (curso 2026–2027). El objetivo es modelar la espera de pacientes por especialidad y aplicar prioridad de emergencias, expiración de turnos regulares, historial de cancelaciones y conteo recursivo.

## Estado del proyecto

La lógica clínica está implementada: registro, atención por prioridad, cancelación manual, expiración y conteo recursivo. `Main` ofrece un menú interactivo por consola con `Scanner` y un reloj simulado. El programa trabaja en memoria: al finalizar la ejecución se pierden sus datos. La presentación oral sigue pendiente.

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
- `registrarPaciente(especialidad, paciente)` valida los datos, rechaza un ID que tenga un turno vigente en cualquier especialidad y registra el turno en ambas estructuras. Si el turno anterior ya venció, se registra como abandono y se admite el nuevo turno.
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
| `Main` | Menú interactivo de operaciones y consultas de la clínica. |

## Abrir y comprobar en IntelliJ IDEA

Abrir la carpeta raíz como proyecto Java y marcar `src/` como **Sources Root** si IntelliJ no lo detecta. Seleccionar un JDK 17 o posterior y ejecutar `Main` para abrir el menú. No se necesita Maven ni Gradle.

En PowerShell también puede compilarse y ejecutarse así:

```powershell
New-Item -ItemType Directory -Force out | Out-Null
javac -encoding UTF-8 --release 17 -d out (Get-ChildItem src\*.java).FullName
java -cp out Main
```

`out/` contiene archivos generados y está excluida de Git.

## Menú de consola

Al ejecutar `Main`, la clínica comienza vacía. El reloj simulado toma la fecha y hora del equipo al iniciar, con precisión de minutos. El tiempo avanza únicamente mediante la opción 8; escribir o consultar no hace avanzar ese reloj. Cada paciente se registra con la hora que muestra el menú.

| Opción | Operación |
| --- | --- |
| 1 | Agregar una especialidad. |
| 2 | Registrar un paciente: especialidad, ID, nombre y tipo (`REGULAR` o `EMERGENCIA`). El tipo acepta mayúsculas y minúsculas. |
| 3 | Mostrar las especialidades y sus pacientes en espera, agrupados por prioridad y en orden FIFO. |
| 4 | Atender al siguiente paciente de una especialidad. |
| 5 | Cancelar manualmente un turno por ID. |
| 6 | Consultar la última cancelación sin extraerla. |
| 7 | Consultar todo el historial de cancelaciones en orden LIFO, conservándolo. |
| 8 | Avanzar una cantidad positiva y entera de minutos y procesar expiraciones. |
| 9 | Consultar los abandonos por especialidad y su total recursivo. |
| 0 | Salir. |

Al registrar o atender, el menú muestra las especialidades disponibles. Escribe su nombre tal como aparece. Para registrar un turno, puedes usar un ID de texto como `P001` o uno numérico como `001`. Para cancelar, escribe el mismo ID que aparece en la lista de espera de la opción 3.

Para probar la prioridad, agregar una especialidad y registrar primero un regular y luego una emergencia; la opción 4 atenderá primero a la emergencia. Para probar la expiración, registrar un regular y avanzar 30 minutos: continúa esperando; avanzar un minuto más lo registra como abandonado. Las consultas de cancelaciones conservan la pila mediante una pila auxiliar con las operaciones de la conferencia.

El menú usa `main`, métodos por acción, `while (!salir)` y `switch` con `case` y `break`, siguiendo el estilo del proyecto anterior del navegador. Lee con `Scanner` y escribe con `System.out`. Si la entrada es inválida o `Clinica` rechaza una operación, el menú muestra el motivo y permite elegir otra opción. El reloj solo acepta avances de minutos enteros y positivos. La opción 0 termina la sesión. Si se cierra la entrada durante una operación, la sesión termina sin registrar los datos incompletos.

## Casos de prueba ejecutados

**Las pruebas se ejecutaron y pasaron.** El proyecto se compiló para Java 17. Durante el desarrollo se probaron las estructuras enlazadas y la lógica clínica con archivos temporales, y se verificó el menú mediante sesiones de consola con entradas válidas e inválidas.

| Caso probado | Resultado observado |
| --- | --- |
| Dos emergencias y dos regulares intercalados | Primero salen las emergencias; dentro de cada tipo se respeta FIFO. |
| Regular con 30 minutos de espera | Continúa en cola. |
| Regular con 31 minutos de espera | Se retira y aumenta en uno el abandono de su especialidad. |
| Emergencia con más de 30 minutos | No expira por esta regla. |
| Cancelación manual | Se retira de la espera, aparece en la cima de la pila y no cuenta como abandono. |
| Clínica o colas vacías | El menú avisa si faltan especialidades o si no hay pacientes en espera. |
| Especialidad o paciente inexistente, ID duplicado | Se rechaza la operación y se conserva el turno válido. |
| Varias especialidades | El conteo recursivo devuelve la suma de sus abandonos. |
| Opción vacía, texto, decimal, entero fuera de rango u opción inexistente | Se muestra un mensaje y se vuelve al menú. |
| Minutos cero, negativos o con formato inválido | Se rechaza el avance y se conserva la hora. |
| Datos obligatorios vacíos, tipo desconocido o especialidad duplicada | Se rechaza la operación y se puede volver a intentar. |
| Fin de entrada en el menú o durante un registro | La sesión finaliza sin un error de ejecución. |

Los casos de la tabla pueden repetirse manualmente desde el menú de consola. También pasaron las comprobaciones de fechas extremas de `LocalDateTime`, rechazo de elementos nulos en pilas y colas, y registro de un ID cuyo turno anterior ya venció.

## Presentación

[PowerPoint del equipo 8](presentacion/Equipo-8-Clinica-de-turnos.pptx). Las nueve diapositivas incluyen el reparto por integrante y los pasos de una demostración de consola. La exposición está organizada para durar 19 minutos y 10 segundos, con 3 minutos y 50 segundos por integrante.

## Declaración de uso de inteligencia artificial

Se utilizó Codex para proponer el diseño de clases, redactar este README, preparar y ejecutar las pruebas básicas, funcionales y de consola, y preparar las diapositivas con la plantilla institucional.
