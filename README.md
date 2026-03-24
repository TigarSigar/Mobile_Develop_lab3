<div align="center">

# МИНИСТЕРСТВО НАУКИ И ВЫСШЕГО ОБРАЗОВАНИЯ  
# РОССИЙСКОЙ ФЕДЕРАЦИИ

## ФЕДЕРАЛЬНОЕ ГОСУДАРСТВЕННОЕ БЮДЖЕТНОЕ  
## ОБРАЗОВАТЕЛЬНОЕ УЧРЕЖДЕНИЕ  
## ВЫСШЕГО ОБРАЗОВАНИЯ  
## «КЕМЕРОВСКИЙ ГОСУДАРСТВЕННЫЙ УНИВЕРСИТЕТ»

### Институт цифры

<br><br>

# ОТЧЁТ  
# О ВЫПОЛНЕНИИ ЛАБОРАТОРНОЙ РАБОТЫ №3

<br>

### по дисциплине: Разработка мобильных приложений  
### тема: Фоновые задачи в Android. Реализация приложения **TDT (ToDoTime)**

<br><br>

**Студент:** 3 курса, группы ФИТ-231  
**Кононенко Егор Сергеевич**

**Направление подготовки:**  
02.03.02 — Фундаментальная информатика и информационные технологии

<br><br><br>

**Руководитель:**  
асс. Киселёв К. Е.

<br><br><br><br>

**Кемерово 2025**

</div>

---

# Лабораторная работа №3  
## Фоновые задачи в Android. Реализация приложения TDT (ToDoTime)

## Цель работы

Изучить основные способы организации фоновых задач в Android и применить их на практике при разработке мобильного приложения. Освоить использование `WorkManager`, `Broadcast Receiver`, `Content Provider`, локальной базы данных `Room`, а также реализовать синхронизацию данных с REST API и систему уведомлений.

---

## Краткая теоретическая часть

При разработке Android-приложений часто возникает необходимость выполнять действия в фоне: синхронизировать данные, отслеживать изменения сети, реагировать на системные события и предоставлять доступ к данным другим приложениям.

Для решения таких задач в Android используются следующие механизмы:

### 1. WorkManager

`WorkManager` применяется для выполнения отложенных и периодических фоновых задач. Это основной рекомендуемый способ запуска фоновой работы, если задача должна гарантированно выполниться даже после перезапуска приложения или устройства. В данной лабораторной работе `WorkManager` используется для периодической синхронизации списка задач.

### 2. Broadcast Receiver

`Broadcast Receiver` позволяет приложению реагировать на системные события. Например, можно отследить восстановление интернет-соединения и после этого инициировать синхронизацию данных. В рамках данной работы этот механизм используется для запуска немедленной синхронизации после появления сети.

### 3. Content Provider

`Content Provider` нужен для предоставления доступа к данным приложения другим приложениям. Через него можно организовать чтение и добавление записей извне. В данной лабораторной работе через `Content Provider` предоставляется доступ к задачам.

### 4. Room

`Room` — это библиотека для работы с локальной SQLite-базой данных на более удобном и безопасном уровне. Она позволяет хранить задачи в памяти устройства, выполнять запросы через DAO и использовать сущности для описания таблиц.

### 5. Уведомления

Уведомления позволяют информировать пользователя о важных событиях в приложении. В данной лабораторной работе уведомления используются после завершения синхронизации и после добавления новой задачи извне.

---

## Постановка задания

В рамках лабораторной работы необходимо было разработать To-Do приложение и реализовать следующие возможности:

- локальное хранение задач с использованием `Room`;
- имитацию синхронизации через REST API с использованием `jsonplaceholder.typicode.com`;
- периодическую синхронизацию каждые 15 минут через `WorkManager` при наличии интернет-соединения;
- отслеживание восстановления сети через `Broadcast Receiver`;
- запуск одноразовой синхронизации при появлении интернета;
- предоставление доступа к задачам через `Content Provider`;
- показ уведомлений после синхронизации и при добавлении задачи извне.

В качестве практической реализации было разработано приложение **TDT (ToDoTime)**.

---

## Описание приложения TDT (ToDoTime)

**TDT (ToDoTime)** — это мобильное Android-приложение для хранения и отображения задач. Основное назначение приложения — предоставление пользователю удобного локального списка задач с возможностью синхронизации, а также демонстрация механизмов фоновой работы Android.

В приложении реализованы основные сценарии:

- просмотр списка задач;
- добавление новой задачи;
- хранение задач в локальной базе данных;
- получение данных из внешнего REST API;
- автоматическая фоновая синхронизация;
- реакция на изменение состояния сети;
- доступ к задачам извне через `Content Provider`;
- отображение уведомлений о событиях.

---

## Используемые технологии

При выполнении лабораторной работы были использованы следующие технологии и компоненты:

- **Kotlin** — основной язык разработки;
- **Jetpack Compose** или XML-интерфейс — для пользовательского интерфейса;
- **Room** — для локального хранения задач;
- **Retrofit** / `HttpURLConnection` / иной HTTP-клиент — для обращения к REST API;
- **WorkManager** — для периодической и одноразовой фоновой синхронизации;
- **Broadcast Receiver** — для отслеживания состояния сети;
- **Content Provider** — для доступа к данным приложения извне;
- **NotificationManager** — для отправки уведомлений.

---

# Реализация лабораторной работы

## 1. Главный экран приложения

На главном экране отображается список задач, сохранённых в локальной базе данных. Пользователь может просматривать существующие задачи и добавлять новые.

Если база данных уже содержит записи, они отображаются в списке сразу после запуска приложения. Если задач нет, приложение показывает пустой список до первого добавления или синхронизации.

![photo_1_2026-03-24_14-41-55](https://github.com/user-attachments/assets/fae12528-337a-497e-ac3f-307170edcee7)

---

## 2. Добавление новой задачи

В приложении предусмотрен механизм добавления новой задачи. После ввода названия задача сохраняется в локальной базе данных `Room` и появляется в общем списке.

Это подтверждает, что приложение работает с локальным хранилищем и поддерживает изменение данных без обращения к серверу.

![photo_3_2026-03-24_14-37-55](https://github.com/user-attachments/assets/b8afe0f0-1c0b-43cd-a7a3-fd316bb34a8f)

---

## 3. Локальное хранение данных через Room

Для хранения задач была реализована база данных `Room`.

Структура хранения включает:

- сущность задачи;
- DAO-интерфейс для работы с запросами;
- класс базы данных;
- репозиторий или слой доступа к данным.

Каждая задача содержит основные поля, например:

- идентификатор;
- название;
- признак выполнения;
- дополнительные служебные данные при необходимости.

Использование `Room` позволило организовать устойчивое локальное хранение данных и обеспечить их сохранность между перезапусками приложения.

**Кодовое подтверждение (без скриншота):**

```kotlin
// TaskEntity.kt
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val isCompleted: Boolean = false,
    val tag: String = "intel",
    val hasTimer: Boolean = false,
    val timeSpentSeconds: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)
```

```kotlin
// TaskDao.kt
@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)
}
```

```kotlin
// AppDatabase.kt
@Database(entities = [TaskEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
```

---

## 4. Синхронизация через REST API

Для имитации работы с удалённым сервером использовался сервис:

`https://jsonplaceholder.typicode.com`

Из внешнего API загружались тестовые данные, которые затем преобразовывались в формат задач приложения и сохранялись в локальную базу.

Такой подход позволил реализовать учебную синхронизацию без необходимости создавать собственный сервер.

В процессе выполнения синхронизации приложение:

1. отправляет запрос к REST API;
2. получает список тестовых задач;
3. обрабатывает полученный JSON;
4. сохраняет данные в локальную базу `Room`;
5. обновляет интерфейс;
6. сохраняет время последней синхронизации.

![photo_2_2026-03-24_14-37-55](https://github.com/user-attachments/assets/468f6b3b-7a49-4beb-9bfa-14931da53a84)

---

## 5. Реализация периодической синхронизации через WorkManager

Одним из ключевых требований лабораторной работы была организация периодической синхронизации каждые 15 минут.

Для этого был использован `WorkManager`, который позволяет запускать фоновые задачи с учётом системных ограничений. В приложении был создан соответствующий `Worker`, выполняющий синхронизацию данных.

При настройке периодической задачи было установлено условие выполнения только при наличии интернет-соединения. Это позволяет избежать бессмысленных попыток обращения к серверу в офлайн-режиме.

Таким образом, `WorkManager` в приложении отвечает за:

- фоновый запуск синхронизации;
- соблюдение интервала;
- проверку наличия сети;
- гарантированное выполнение задачи системой Android.

**Кодовое подтверждение (без скриншота):**

```kotlin
// SyncWorkScheduler.kt
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .build()

val periodicRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
    .setConstraints(constraints)
    .build()

WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "periodic_tasks_sync",
    ExistingPeriodicWorkPolicy.KEEP,
    periodicRequest
)
```

```kotlin
// SyncWorker.kt
override suspend fun doWork(): Result {
    ...
    val now = System.currentTimeMillis()
    SyncPreferences.saveLastSyncTime(applicationContext, now)
    TaskNotifications.showSyncCompleted(...)
    return Result.success()
}
```

![photo_1_2026-03-24_14-43-47](https://github.com/user-attachments/assets/dd221273-0189-4e6c-aacc-f8a5b774b9ff)

---

## 6. Сохранение времени последней синхронизации

После каждого успешного обновления данных приложение сохраняет время последней синхронизации. Это позволяет пользователю видеть, когда именно была выполнена последняя загрузка данных.

Такое решение полезно с практической точки зрения, поскольку делает работу фоновой синхронизации наглядной и позволяет убедиться, что механизм действительно функционирует.

Время синхронизации может отображаться:

- на главном экране;
- в отдельном текстовом поле;
- в настройках приложения;
- в логах.

**Кодовое подтверждение (без скриншота):**

```kotlin
// SyncPreferences.kt
private const val PREFS_NAME = "todo_sync_prefs"
private const val KEY_LAST_SYNC_TIME_MS = "last_sync_time_ms"

fun saveLastSyncTime(context: Context, timestampMs: Long) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putLong(KEY_LAST_SYNC_TIME_MS, timestampMs)
        .apply()
}

fun getLastSyncTime(context: Context): Long {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getLong(KEY_LAST_SYNC_TIME_MS, 0L)
}
```

```kotlin
// SyncWorker.kt
val now = System.currentTimeMillis()
SyncPreferences.saveLastSyncTime(applicationContext, now)
```

---

## 7. Отслеживание сети через Broadcast Receiver

Следующим обязательным требованием было использование `Broadcast Receiver` для отслеживания восстановления интернета.

В приложении был реализован приёмник системного события, связанного с изменением сетевого состояния. Когда устройство снова получает доступ к интернету, приложение автоматически инициирует одноразовую синхронизацию через `WorkManager`.

Это решение позволяет сделать работу приложения более гибкой:

- в отсутствие сети периодическая синхронизация не выполняется;
- после восстановления соединения синхронизация запускается сразу;
- пользователь получает актуальные данные без необходимости выполнять действие вручную.

**Кодовое подтверждение (без скриншота):**

```kotlin
// NetworkReceiver.kt
class NetworkReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        val isConnected = capabilities != null &&
            (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                )

        if (isConnected) {
            SyncWorkScheduler.enqueueOneTimeSync(context, "network_restored")
        }
    }
}
```

```xml
<!-- AndroidManifest.xml -->
<receiver
    android:name=".receiver.NetworkReceiver"
    android:exported="true">
    <intent-filter>
        <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
    </intent-filter>
</receiver>
```

Проверка в коде: при восстановлении сети `isConnected == true` всегда вызывает `enqueueOneTimeSync(...)`, а сама синхронизация выполняется через `WorkManager` с network constraints.

Практическая оговорка: на современных версиях Android системные ограничения на background-broadcast могут влиять на момент доставки `CONNECTIVITY_CHANGE`, поэтому корректность проверяется по факту постановки `OneTimeWorkRequest` в `WorkManager`.

---

## 8. Немедленная одноразовая синхронизация после появления интернета

Помимо периодической работы `WorkManager`, в приложении была реализована одноразовая задача синхронизации, которая запускается при восстановлении сети.

Для этого `Broadcast Receiver` не выполняет тяжёлую работу самостоятельно, а только передаёт управление `WorkManager`, который уже создаёт и запускает `OneTimeWorkRequest`.

Такой подход является правильным, поскольку:

- `Broadcast Receiver` должен работать быстро;
- тяжёлые операции не должны выполняться прямо в обработчике события;
- фоновая задача должна передаваться специализированному механизму.

**Кодовое подтверждение (без скриншота):**

```kotlin
// SyncWorkScheduler.kt
fun enqueueOneTimeSync(context: Context, reason: String) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val request = OneTimeWorkRequestBuilder<SyncWorker>()
        .setConstraints(constraints)
        .setInputData(workDataOf("sync_reason" to reason))
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        "one_time_tasks_sync",
        ExistingWorkPolicy.REPLACE,
        request
    )
}
```

---

## 9. Реализация Content Provider

Одной из самых важных частей лабораторной работы была реализация `Content Provider`, который предоставляет доступ к данным приложения другим приложениям.

В рамках работы `Content Provider` должен поддерживать:

- чтение списка задач;
- добавление новой задачи извне.

Для этого в приложении был реализован собственный провайдер с URI, соответствующим таблице задач. Через него можно получать данные в виде курсора и вставлять новые записи.

Это означает, что другое приложение или тестовый компонент может обратиться к TDT и:

- запросить текущий список задач;
- добавить новую задачу без открытия основного интерфейса.

Именно этот механизм делает данные приложения доступными во внешней среде.

**Кодовое подтверждение (без скриншота):**

```kotlin
// TaskContentProvider.kt
companion object {
    const val AUTHORITY = "com.example.todotime.provider"
    private const val TASKS_ALL = 1
    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, "tasks", TASKS_ALL)
    }
}

override fun query(...): Cursor? = database.taskDao().getAllTasksCursor()

override fun insert(uri: Uri, values: ContentValues?): Uri? {
    ...
    runBlocking { database.taskDao().insert(task) }
    context?.let { TaskNotifications.showExternalTaskAdded(it, task.title) }
    return insertedUri
}
```

Пример внешнего обращения (ADB):

```bash
adb shell content query --uri content://com.example.todotime.provider/tasks
```

```bash
adb shell content insert --uri content://com.example.todotime.provider/tasks \
  --bind title:s:"Задача из внешнего приложения" \
  --bind tag:s:intel \
  --bind hasTimer:b:true
```

---

## 10. Уведомления о завершении синхронизации

После завершения фоновой синхронизации приложение показывает уведомление пользователю. Это сделано для того, чтобы пользователь понимал, что данные были обновлены.

Уведомление может содержать, например:

- сообщение об успешной синхронизации;
- время завершения обновления;
- количество загруженных задач.

Использование уведомлений делает фоновую работу приложения более прозрачной.

**Кодовое подтверждение (без скриншота):**

```kotlin
// SyncWorker.kt
val now = System.currentTimeMillis()
SyncPreferences.saveLastSyncTime(applicationContext, now)
TaskNotifications.showSyncCompleted(
    context = applicationContext,
    contentText = "Последняя синхронизация: ${formatTimestamp(now)}"
)
```

```kotlin
// TaskNotifications.kt
fun showSyncCompleted(context: Context, contentText: String) {
    show(
        context = context,
        channelId = SYNC_CHANNEL_ID,
        title = "Синхронизация завершена",
        message = contentText,
        notificationId = 101
    )
}
```

```kotlin
// MainActivity.kt (Android 13+)
private fun ensureNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    ) return
    requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
}
```

Примечание: на Android 13+ уведомления отображаются только после выдачи пользователем разрешения `POST_NOTIFICATIONS`.

---

## 11. Уведомления при добавлении новой задачи из другого приложения

Отдельно было реализовано уведомление, которое появляется при добавлении новой задачи через `Content Provider`.

Это подтверждает, что:

- внешнее взаимодействие с приложением действительно работает;
- приложение корректно реагирует на изменение данных извне;
- пользователь информируется о внешнем добавлении новой записи.

Такой сценарий особенно важен именно для этой лабораторной работы, потому что он демонстрирует не только наличие `Content Provider`, но и факт его практического использования.

**Кодовое подтверждение (без скриншота):**

```kotlin
// TaskContentProvider.kt
context?.let { ctx ->
    TaskNotifications.showExternalTaskAdded(ctx, task.title)
}
```

```kotlin
// TaskNotifications.kt
fun showExternalTaskAdded(context: Context, taskTitle: String) {
    show(
        context = context,
        channelId = EXTERNAL_TASK_CHANNEL_ID,
        title = "Новая внешняя задача",
        message = "Добавлена задача: $taskTitle",
        notificationId = 200 + Random.nextInt(1, 1000)
    )
}
```

---

# Контрольное задание для самопроверки

В рамках лабораторной работы было выполнено контрольное задание по созданию To-Do приложения, удовлетворяющего требованиям задания.

## Выполненные пункты самопроверки

### 1. Локальное хранение через Room — выполнено

В приложении реализована локальная база данных `Room`, в которой хранятся задачи. Добавленные пользователем записи сохраняются и отображаются после повторного запуска приложения.

### 2. Синхронизация через REST API — выполнено

Для получения тестовых данных используется `jsonplaceholder.typicode.com`. Полученные данные преобразуются в формат задач и сохраняются локально.

### 3. Периодическая синхронизация через WorkManager каждые 15 минут — выполнено

Создана периодическая задача через `WorkManager`, которая выполняется только при наличии интернет-соединения.

### 4. Сохранение времени последней синхронизации — выполнено

После успешного обновления данных фиксируется время последней синхронизации, которое может быть выведено пользователю.

### 5. Broadcast Receiver для отслеживания сети — выполнено

При восстановлении подключения приложение получает системное событие и инициирует синхронизацию.

### 6. Немедленная одноразовая синхронизация после появления интернета — выполнено

После появления сети запускается `OneTimeWorkRequest`, который выполняет срочное обновление данных.

### 7. Content Provider для чтения и добавления задач извне — выполнено

Реализован `Content Provider`, поддерживающий получение списка задач и вставку новых записей из внешнего приложения или тестового компонента.

### 8. Уведомления после синхронизации и внешнего добавления задачи — выполнено

Приложение информирует пользователя о завершении синхронизации и о добавлении новой задачи извне.

---

## Итог по контрольному заданию

Таким образом, контрольное задание для самопроверки выполнено полностью. Разработанное приложение **TDT (ToDoTime)** соответствует поставленным требованиям и демонстрирует использование основных механизмов фоновой работы Android на практике.

---

## Вывод

В ходе выполнения лабораторной работы были изучены и применены основные инструменты Android для организации фоновых задач и межкомпонентного взаимодействия.

В результате было разработано приложение **TDT (ToDoTime)**, в котором реализованы:

- локальное хранение задач через `Room`;
- синхронизация данных через REST API;
- периодическая фоновая работа через `WorkManager`;
- реакция на изменение сети через `Broadcast Receiver`;
- доступ к данным через `Content Provider`;
- система уведомлений о значимых событиях.

Практическая часть показала, что `WorkManager` является наиболее удобным и правильным инструментом для запуска периодических и отложенных фоновых задач, `Broadcast Receiver` подходит для реакции на системные события, а `Content Provider` используется в ситуациях, когда нужно предоставить доступ к данным приложения другим программам.

Выполненная лабораторная работа позволила закрепить навыки разработки Android-приложений и глубже понять механизмы, используемые для организации фоновой работы и обмена данными в мобильных системах.

---

## Приложение: кодовые доказательства вместо скриншотов

1. `Room` и локальная модель: `TaskEntity`, `TaskDao`, `AppDatabase`.
2. REST-клиент: `JsonPlaceholderApi`, `JsonPlaceholderClient`.
3. Периодическая синхронизация: `SyncWorkScheduler.schedulePeriodicSync`.
4. Логика worker: `SyncWorker.doWork`.
5. Сохранение времени последней синхронизации: `SyncPreferences`.
6. Отслеживание сети: `NetworkReceiver`.
7. OneTime-синхронизация: `SyncWorkScheduler.enqueueOneTimeSync`.
8. Доступ извне: `TaskContentProvider`.
9. События уведомлений: `TaskNotifications`, вызовы из `SyncWorker` и `TaskContentProvider`.

Скриншоты остаются полезными только для UI-части (главный экран/добавление задачи), но для функциональных пунктов ТЗ в отчёте достаточно этих кодовых фрагментов.

---
