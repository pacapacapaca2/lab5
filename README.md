# Лабораторная работа №5. Хранение данных. Настройки и внешние файлы
- _Выполнила:_ Шереметова
- _Язык программирования:_ Java
- _API:_ https://ntv.ifmo.ru/file/journal/идентификатор_журнала.pdf

## Что делает приложение?
Приложение состоит из 1 экрана, который позволяет:
- вводить номер нужного журнала
- скачивать этот журнал
- переходить к просмотру в подходящем приложении
- удалять файл из памяти устройства.

_**Примечания**:_
- _кнопки "Смотреть" и "Удалить" становятся активными только после успешного скачивания файла журнала._
- _на ветке `instruction` содержится код для вывода уведомления с краткой инструкцией по использованию приложения (`popupWindow`)_
<p align="center">
    <img src="https://github.com/user-attachments/assets/0e164ae5-1a87-4d59-83eb-fac69c44d7a0" width="350"> 
</p> 

---

## Скачивание файла или действия после нажатия на кнопку "Скачать"
### 1. Проверка на получение идентификатора журнала
- если пользователь не указал ID журнала, то выведется сообщение _"Пожалуйста, введите номер журнала"_
<p align="center">
<img src="https://github.com/user-attachments/assets/1e044d5c-78fe-49e5-8ed6-1ab202dd3df1" width="200"> 
</p>
  
- если пользователь указал ID журнала, то продолжится выполнение загрузки и выведется сообщение _"Файл загружается..."_
<p align="center">
<img src="https://github.com/user-attachments/assets/d0aa464d-5949-492d-9662-f4158b9f5d78" width="200"> 
</p>

### 2. Создание папки для хранения файлов
- если Android 10+ версии, то приложению не требуется запрашивать дополнительные разрешения на доступ к хранилищу и достаточно следующего кода:
```java
private void createDirectoryAndDownload(String journalId) {
        File dir = new File(getExternalFilesDir(null), "JournalDownloads");
        if (!dir.exists()) {
            boolean isCreated = dir.mkdirs();  // Создаем директорию, если ее нет
            if (isCreated) {
                Log.d("Directory", "Directory created successfully");
            } else {
                Log.d("Directory", "Failed to create directory");
                Toast.makeText(this, "Ошибка при создании директории!", Toast.LENGTH_SHORT).show();
                return;  // Выход, если не удалось создать директорию
            }
        }
        // Запуск задачи по загрузке файла
        String fileUrl = "https://ntv.ifmo.ru/file/journal/" + journalId + ".pdf";
        new DownloadFileTask(MainActivity.this).execute(fileUrl, journalId);
    }
```
### 3. Скачивание файла
- если по указанному ID не находится журнал, то выведется сообщение _"Журнал не найден!"_
<p align="center">
<img src="https://github.com/user-attachments/assets/b83d86e3-cf92-47b3-b3a8-0e5bf18baf16" width="200"> 
</p>
- если по указанному ID найден журнал и скачивание успешно, то все кнопки становятся активными и выводится сообщение _"Скачивание файла завершено!"_
<p align="center">
<img src="https://github.com/user-attachments/assets/ed09ff05-aeea-44f1-b358-ab239481ac54" width="200"> 
</p>

---
## Просмотр загруженного файла
По нажатию на "Смотреть" откроется ботомшит с выбором подходящего приложения
<p align="center">
<img src="https://github.com/user-attachments/assets/d2140e28-af3f-44ed-aa27-0a7094031608" width="200"> 
</p>

## Удаление файла
По нажатию на "Удалить" файл удаляется из памяти устройства и выводится сообщение _"Файл удален"_
<p align="center">
<img src="https://github.com/user-attachments/assets/53184794-bdec-4661-921d-2751aa8aa9a8" width="200"> 
</p>
