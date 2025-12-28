# AnimatedChatMod

Клиентский Forge-мод для Minecraft.
Добавляет анимации в чат.

---

## RU

### Что это
Мод отображает анимированный текст в чате.
Работает на обычных серверах, без серверного плагина.

### Формат анимации
\<animation:interval:2><->Кадр</-><-><red>Текст</red></->\</animation>

### Интервал
Интервал указывается в тиках.
1 тик = 50 мс.

### Настройки
Файл:
config/animatedchat-client.toml

Основные параметры:
- enable-animations — включить анимации
- history-size — размер истории сообщений
- max-chat-length — максимальная длина ввода

---

## EN

### What is this
Client-side Forge mod.
Adds animated text to chat.
Works on vanilla servers, no plugin required.

### Animation format
\<animation:interval:2><->Frame</-><-><red>Text</red></->\</animation>

### Interval
Interval is in ticks.
1 tick = 50 ms.

### Config
File:
config/animatedchat-client.toml

Main options:
- enable-animations — enable animations
- history-size — chat history size
- max-chat-length — max input length

---

## Notes
- Client-side only
- Animations depend on received chat text
- Some servers may modify messages
