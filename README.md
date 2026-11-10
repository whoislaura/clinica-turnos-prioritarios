# Clínica de turnos prioritarios

Proyecto del **equipo 8** para el segundo examen parcial de Estructuras de Datos (curso 2026–2027). El objetivo es modelar la espera de pacientes por especialidad y aplicar prioridad de emergencias, expiración de turnos regulares, historial de cancelaciones y conteo recursivo.

## Estado del proyecto

Este repositorio contiene **la estructura inicial de clases**, no el sistema terminado. Las operaciones básicas de la pila, la cola y la inserción en la multilista están implementadas. Los métodos de coordinación clínica en `Clinica` aún lanzan `UnsupportedOperationException`. No existe todavía una interfaz de atención, ni pruebas funcionales del sistema, ni presentación oral.

## Reglas del equipo 8

- **Multilista:** `Clínica → especialidades o áreas → pacientes en espera`. Cada paciente tiene ID único, nombre, tipo (`REGULAR` o `EMERGENCIA`) y fecha/hora de registro. Se usa `LocalDateTime` para conservar hora y minuto y calcular esperas que crucen la medianoche.
- **Cola dual:** cada especialidad tendrá una cola de emergencias y otra de regulares. Siempre se atenderá primero a una emergencia. Dentro de cada cola se conservará el orden de llegada (FIFO).
- **Expiración:** un paciente regular que lleve **más de 30 minutos** sin atención será retirado de la espera y registrado como abandonado por tiempo excedido. A los 30 minutos exactos todavía no expira. La regla no se aplica a emergencias.
- **Pila:** solo las cancelaciones manuales de turnos se guardarán en orden LIFO. Una cancelación manual no aumentará el conteo de abandonos por tiempo.
- **Recursividad:** un método recorrerá los nodos externos de las especialidades y sumará sus abandonos, con `null` como caso base y sin `for` ni `while` en ese cálculo.

La sublista de la multilista y las dos colas representan a los mismos pacientes en espera. Las futuras operaciones de registrar, atender, cancelar y expirar deberán actualizar ambas estructuras juntas para evitar datos inconsistentes. Las estructuras enlazadas se implementan con nodos propios, sin `java.util.Queue`, `Stack`, `ArrayList` ni `LinkedList`.

## Clases

| Clase | Responsabilidad |
| --- | --- |
| `Node<E>` | Nodo genérico `info`/`next` usado por pila y colas, como en las conferencias 3 y 4. |
| `LinkedQueue<E>` | Cola enlazada con `first`/`last` y operaciones `add`, `poll`, `peek`, `isEmpty`. |
| `LinkedStack<E>` | Pila enlazada con `top` y operaciones `push`, `pop`, `top`, `isEmpty`. |
| `NodoExterno` | Nodo de especialidad con referencia a su sublista y al siguiente nodo externo. |
| `NodoInterno` | Nodo de paciente con referencia al siguiente paciente de su especialidad. |
| `Multilista` | Cabeza de especialidades e inserción y consulta de sus sublistas, según la conferencia 5. |
| `Paciente` | Datos del paciente y tipo de turno. |
| `Especialidad` | Nombre, dos colas de espera y número de abandonos por tiempo. |
| `Cancelacion` | Paciente, especialidad y momento de una cancelación manual. |
| `Clinica` | Coordinará las estructuras y contendrá las operaciones clínicas y el conteo recursivo. |
| `Main` | Punto de entrada; por ahora solo informa el estado inicial. |
| `PruebaEstructuras` | Comprobaciones básicas de pila, cola y multilista. |

## Abrir y comprobar en IntelliJ IDEA

Abrir la carpeta raíz como proyecto Java y marcar `src/` como **Sources Root** si IntelliJ no lo detecta. Seleccionar un JDK 17 o posterior y ejecutar `Main` o `PruebaEstructuras` con las aserciones habilitadas (`-ea`). No se necesita Maven ni Gradle.

En PowerShell también puede compilarse y ejecutarse así:

```powershell
New-Item -ItemType Directory -Force out | Out-Null
javac -encoding UTF-8 -d out (Get-ChildItem src\*.java).FullName
java -ea -cp out PruebaEstructuras
java -cp out Main
```

`out/` contiene archivos generados y está excluida de Git.

## Casos de prueba requeridos para completar el examen

| Caso | Resultado esperado |
| --- | --- |
| Dos emergencias y dos regulares intercalados | Primero salen las emergencias; dentro de cada tipo se respeta FIFO. |
| Regular con 30 minutos de espera | Continúa en cola. |
| Regular con 31 minutos de espera | Se retira y aumenta en uno el abandono de su especialidad. |
| Emergencia con más de 30 minutos | No expira por esta regla. |
| Cancelación manual | Se retira de la espera, aparece en la cima de la pila y no cuenta como abandono. |
| Clínica o colas vacías | Se informa que no hay paciente para atender, sin fallar. |
| Especialidad o paciente inexistente, ID duplicado | Se rechaza la operación con un mensaje claro, sin alterar las estructuras. |
| Varias especialidades | El conteo recursivo devuelve la suma de sus abandonos. |

Solo se han preparado comprobaciones básicas de estructuras; **los casos funcionales de esta tabla aún no se han ejecutado**.

## Entrega pendiente

La orden exige un proyecto Java funcional con pruebas positivas, negativas y de borde; un README documentado; una presentación en PDF o diapositivas incluida o enlazada; y una exposición oral de 18–20 minutos. Antes de entregar habrá que implementar las operaciones de `Clinica`, ejecutar y documentar las pruebas funcionales y añadir la presentación.

## Declaración de uso de inteligencia artificial

Se utilizó Codex para interpretar la orden y las conferencias, proponer el diseño inicial de clases, escribir este README y preparar el esqueleto Java y las comprobaciones básicas. La lógica clínica funcional, sus pruebas y la presentación siguen pendientes; esta declaración se actualizará conforme avance el trabajo.
