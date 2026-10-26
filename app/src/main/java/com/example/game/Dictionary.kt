package com.example.game

data class WordDefinition(
    val word: String,          // Uppercase word
    val definition: String,    // Clue or hint
    val category: String = ""  // Thematic category badge
)

object Dictionary {
    const val WILDCARD_CHAR = '★'

    val RU_WORDS = listOf(
        // Быт и Дом
        WordDefinition("ДОМ", "Место, где человек живёт и чувствует себя в безопасности", "Быт"),
        WordDefinition("КОТ", "Домашний пушистый зверёк, любящий мурлыкать и ловить мышей", "Природа"),
        WordDefinition("ОКНО", "Проём в стене со стеклом для дневного света и свежего воздуха", "Быт"),
        WordDefinition("ЛАМПА", "Искусственный источник света в стеклянном корпусе", "Быт"),
        WordDefinition("ШАРФ", "Длинная полоса ткани, обматываемая вокруг шеи для тепла", "Быт"),
        WordDefinition("КЛЮЧ", "Металлический стержень с бородкой для отпирания замка", "Быт"),
        WordDefinition("ЧАСЫ", "Прибор для точного отсчёта текущего времени суток", "Быт"),
        WordDefinition("СТОЛ", "Предмет мебели на ножках с широкой горизонтальной крышкой", "Быт"),
        WordDefinition("ХЛЕБ", "Основной пищевой продукт, выпекаемый из мучного теста", "Кулинария"),
        WordDefinition("БОРЩ", "Традиционный наваристый свекольный суп красного цвета", "Кулинария"),
        WordDefinition("ЛИМОН", "Кислый цитрусовый плод ярко-жёлтого цвета", "Кулинария"),
        WordDefinition("ПИЦЦА", "Итальянская круглая лепёшка с томатами, сыром и начинкой", "Кулинария"),
        WordDefinition("КОФЕ", "Бодрящий ароматный напиток из обжаренных зёрен", "Кулинария"),

        // Природа и Стихии
        WordDefinition("ЛЕС", "Огромная природная территория, густо заросшая деревьями", "Природа"),
        WordDefinition("МОРЕ", "Большой водоём с солёной водой, соединённый с океаном", "Стихии"),
        WordDefinition("РЕКА", "Природный водный поток, текущий в разработанном им русле", "Природа"),
        WordDefinition("ВОДА", "Прозрачная жидкость без цвета и запаха — основа жизни", "Стихии"),
        WordDefinition("ОГОНЬ", "Раскалённые светящиеся газы, выделяемые при горении", "Стихии"),
        WordDefinition("ВЕТЕР", "Поток воздуха, быстро движущийся параллельно земной поверхности", "Стихии"),
        WordDefinition("ГРОЗА", "Атмосферное явление с молниями, громом и ливнем", "Стихии"),
        WordDefinition("СНЕГ", "Твёрдые атмосферные осадки в виде пушистых белых кристаллов", "Стихии"),
        WordDefinition("ВОЛНА", "Водяной вал, образующийся от колебания поверхности моря", "Стихии"),
        WordDefinition("ГОРА", "Значительная возвышенность, высоко поднимающаяся над равниной", "Природа"),
        WordDefinition("ТИГР", "Крупный полосатый хищник семейства кошачьих", "Природа"),
        WordDefinition("ОРЁЛ", "Крупная хищная птица с острым зрением, парящая в вышине", "Природа"),
        WordDefinition("ОРЕЛ", "Крупная хищная птица с острым зрением, парящая в вышине", "Природа"),
        WordDefinition("РОЗА", "Благородный цветок с пышным бутоном и острыми шипами", "Природа"),

        // Космос и Наука
        WordDefinition("ЗЕМЛЯ", "Третья планета от Солнца, наш общий голубой дом", "Космос"),
        WordDefinition("ЛУНА", "Естественный небесный спутник Земли, сияющий в ночи", "Космос"),
        WordDefinition("МАРС", "Четвёртая планета Солнечной системы, прозванная Красной", "Космос"),
        WordDefinition("АТОМ", "Мельчайшая химически неделимая частица вещества", "Наука"),
        WordDefinition("КВАНТ", "Неделимая порция какой-либо физической величины в физике", "Наука"),
        WordDefinition("ЛАЗЕР", "Оптический генератор узконаправленного пучка когерентного света", "Наука"),
        WordDefinition("ЗВЕЗДА", "Массивное самосветящееся небесное тело из раскалённой плазмы", "Космос"),
        WordDefinition("КОМЕТА", "Небесное тело с ярким хвостом из пыли и газа", "Космос"),
        WordDefinition("СВЕТ", "Электромагнитное излучение, воспринимаемое человеческим глазом", "Наука"),
        WordDefinition("ЗВУК", "Упругие волны, распространяющиеся в среде и слышимые ухом", "Наука"),

        // Технологии и Город
        WordDefinition("ИГРА", "Занятие для развлечения, отдыха или интеллектуального турнира", "Игры"),
        WordDefinition("КОД", "Набор команд и символов, написанный программистом", "Технологии"),
        WordDefinition("СЕТЬ", "Глобальное соединение компьютеров для обмена данными", "Технологии"),
        WordDefinition("ЭКРАН", "Светящаяся панель устройства для отображения графики", "Технологии"),
        WordDefinition("РОБОТ", "Автоматическое устройство, выполняющее сложные операции", "Технологии"),
        WordDefinition("РАДАР", "Система обнаружения объектов с помощью радиоволн", "Технологии"),
        WordDefinition("ТАКСИ", "Автомобиль для перевозки пассажиров по индивидуальному маршруту", "Город"),
        WordDefinition("МЕТРО", "Скоростная подземная городская железная дорога", "Город"),
        WordDefinition("ГОРОД", "Крупный населённый пункт с проспектами и архитектурой", "Город"),
        WordDefinition("МОСТ", "Инженерное сооружение, перекинутое через реку или ущелье", "Город"),
        WordDefinition("ПОЕЗД", "Состав сцепленных железнодорожных вагонов с локомотивом", "Город"),

        // Культура, Искусство и Общество
        WordDefinition("КНИГА", "Сброшюрованные листы с текстом под единой обложкой", "Культура"),
        WordDefinition("СЛОВО", "Единица языка, служащая для выражения мысли и понятия", "Культура"),
        WordDefinition("БУКВА", "Графический символ, входящий в состав алфавита", "Культура"),
        WordDefinition("ТЕМА", "Главный предмет беседы, исследования или размышления", "Культура"),
        WordDefinition("ПЕСНЯ", "Музыкальное вокальное произведение со стихотворным текстом", "Искусство"),
        WordDefinition("РИТМ", "Равномерное чередование звуковых или визуальных элементов", "Искусство"),
        WordDefinition("КИНО", "Вид современного искусства, создающий движущиеся картины", "Искусство"),
        WordDefinition("ТЕАТР", "Сценическое искусство и здание, где играют спектакли", "Искусство"),
        WordDefinition("ДРУГ", "Близкий человек, связанный с вами доверием и преданностью", "Общество"),
        WordDefinition("ВРЕМЯ", "Необратимое течение из прошлого через настоящее в будущее", "Философия"),
        WordDefinition("ЖИЗНЬ", "Высшая форма существования материи и биения сердца", "Философия"),
        WordDefinition("МЕЧТА", "Заветное желание, устремлённое в будущее", "Философия"),
        WordDefinition("УСПЕХ", "Блестящий результат, победа и достижение поставленной цели", "Спорт"),
        WordDefinition("СТАРТ", "Начальный момент и линия запуска спортивного состязания", "Спорт")
    )

    val EN_WORDS = listOf(
        // Home & Daily Life
        WordDefinition("CAT", "A small domesticated feline mammal known for purring", "Nature"),
        WordDefinition("DOG", "Man's loyal four-legged best friend", "Nature"),
        WordDefinition("HOME", "The cozy place where one lives permanently", "Daily"),
        WordDefinition("BOOK", "A written or printed work consisting of bound pages", "Culture"),
        WordDefinition("LAMP", "A device providing an artificial source of light", "Daily"),
        WordDefinition("DOOR", "A hinged barrier used to close an opening in a wall", "Daily"),
        WordDefinition("KEY", "A shaped metal instrument used to open a lock", "Daily"),
        WordDefinition("TIME", "The indefinite continued progress of existence and events", "Philosophy"),
        WordDefinition("BREAD", "Baked food made of flour, water, and yeast", "Food"),
        WordDefinition("LEMON", "A bright yellow citrus fruit with sour juice", "Food"),
        WordDefinition("PIZZA", "Italian flatbread baked with tomato sauce and cheese", "Food"),

        // Nature & Elements
        WordDefinition("RAIN", "Atmospheric moisture condensed into falling drops", "Elements"),
        WordDefinition("FIRE", "Combustion emitting heat, flame, and glowing light", "Elements"),
        WordDefinition("WIND", "The perceptible natural movement of air currents", "Elements"),
        WordDefinition("SNOW", "Atmospheric water vapor frozen into ice crystals", "Elements"),
        WordDefinition("WAVE", "A moving ridge on the surface of the ocean", "Elements"),
        WordDefinition("WATER", "A transparent liquid forming the world's streams and seas", "Elements"),
        WordDefinition("STORM", "Violent weather with thunder, lightning, and gale winds", "Elements"),
        WordDefinition("TREE", "A tall perennial woody plant with a trunk and branches", "Nature"),
        WordDefinition("ROSE", "A fragrant flower with soft petals and thorny stems", "Nature"),
        WordDefinition("WOLF", "A wild carnivorous mammal of the dog family", "Nature"),
        WordDefinition("HAWK", "A fast-flying bird of prey with sharp vision", "Nature"),

        // Space & Science
        WordDefinition("STAR", "A luminous sphere of plasma held together by gravity in space", "Space"),
        WordDefinition("MOON", "Earth's natural orbital satellite glowing by reflected sunlight", "Space"),
        WordDefinition("MARS", "The dusty fourth planet from the Sun, known as the Red Planet", "Space"),
        WordDefinition("COMET", "An icy cosmic body that releases a bright tail near the Sun", "Space"),
        WordDefinition("ORBIT", "The curved gravitationally bound path of a planet or spacecraft", "Space"),
        WordDefinition("ATOM", "The fundamental building block of chemical elements", "Science"),
        WordDefinition("LASER", "A device that emits an intense focused beam of coherent light", "Science"),
        WordDefinition("LIGHT", "Electromagnetic radiation visible to the human eye", "Science"),
        WordDefinition("SOUND", "Vibrations that travel through the air as audible acoustic waves", "Science"),

        // Tech & City
        WordDefinition("GAME", "A structured form of play or competitive puzzle", "Gaming"),
        WordDefinition("WORD", "A single distinct meaningful element of speech or writing", "Culture"),
        WordDefinition("CODE", "Instructions written in a programming language", "Tech"),
        WordDefinition("DATA", "Facts and digital information stored and processed by computers", "Tech"),
        WordDefinition("PIXEL", "The smallest controllable illuminated element of a screen", "Tech"),
        WordDefinition("ROBOT", "A programmable machine capable of carrying out complex tasks", "Tech"),
        WordDefinition("RADAR", "Detection system using radio waves to locate objects", "Tech"),
        WordDefinition("CITY", "A large metropolitan town with dense architecture", "City"),
        WordDefinition("TRAIN", "A series of connected railway carriages pulled along a track", "City"),
        WordDefinition("PLANE", "A powered flying vehicle with fixed wings", "City"),
        WordDefinition("PHONE", "A smart mobile communication device", "Tech"),

        // Art & Mind
        WordDefinition("SONG", "A musical composition intended to be performed by the voice", "Art"),
        WordDefinition("MUSIC", "Vocal or instrumental sounds combined to produce harmony", "Art"),
        WordDefinition("DANCE", "Rhythmic movement of the body, usually to music", "Art"),
        WordDefinition("SMILE", "A pleased, kind, or amused facial expression", "Mind"),
        WordDefinition("DREAM", "A cherished aspiration, ambition, or vision", "Mind"),
        WordDefinition("LIFE", "Vital existence, growth, and consciousness", "Philosophy"),
        WordDefinition("LOVE", "An intense feeling of deep affection and devotion", "Mind"),
        WordDefinition("QUEST", "A long or arduous search for something of great value", "Gaming")
    )

    fun getDefinitionsForLanguage(isRussian: Boolean): List<WordDefinition> {
        return if (isRussian) RU_WORDS else EN_WORDS
    }

    /**
     * Checks if a candidate string (which may contain '★' wildcard characters) matches a valid word.
     */
    fun isValidWord(word: String, isRussian: Boolean): Boolean {
        return findFirstMatchingDefinition(word, isRussian) != null
    }

    /**
     * Finds the first matching WordDefinition for a string that may contain '★' wildcards.
     */
    fun findFirstMatchingDefinition(pattern: String, isRussian: Boolean): WordDefinition? {
        val clean = pattern.uppercase().trim()
        if (clean.length < 3) return null
        val list = getDefinitionsForLanguage(isRussian)
        if (!clean.contains(WILDCARD_CHAR)) {
            return list.firstOrNull { it.word == clean }
        }
        return list.firstOrNull { def ->
            def.word.length == clean.length && def.word.indices.all { idx ->
                clean[idx] == WILDCARD_CHAR || clean[idx] == def.word[idx]
            }
        }
    }

    /**
     * Retrieves definition for a given word if matches.
     */
    fun getDefinition(word: String, isRussian: Boolean): String? {
        return findFirstMatchingDefinition(word, isRussian)?.definition
    }

    /**
     * Retrieves category for a given word if matches.
     */
    fun getCategory(word: String, isRussian: Boolean): String {
        val def = findFirstMatchingDefinition(word, isRussian)
        if (def != null && def.category.isNotBlank()) return def.category
        return if (isRussian) "Общее" else "General"
    }
}
