package com.example.game

enum class WordTheme {
    ALL,
    HOME_COOKING,
    NATURE_ANIMALS,
    FAIRY_TALES_KIDS
}

data class WordDefinition(
    val word: String,          // Uppercase word
    val definition: String,    // Clue or hint
    val category: String = "", // Thematic category badge
    val themeGroup: WordTheme = WordTheme.ALL
)

object Dictionary {
    const val WILDCARD_CHAR = '★'

    val RU_WORDS = listOf(
        // 🏡 Уютный Дом и Кулинария (Для мам, бабушек и всей семьи)
        WordDefinition("ДОМ", "Уютное место, где живёт дружная семья и всегда тепло", "Дом и Уют", WordTheme.HOME_COOKING),
        WordDefinition("ЧАЙ", "Душистый согревающий напиток для вечерней беседы всей семьёй", "Кулинария", WordTheme.HOME_COOKING),
        WordDefinition("ПИРОГ", "Румяная домашняя выпечка с яблоками, ягодами или капустой", "Кулинария", WordTheme.HOME_COOKING),
        WordDefinition("БЛИН", "Круглое золотистое угощение со сковороды, похожее на солнышко", "Кулинария", WordTheme.HOME_COOKING),
        WordDefinition("МЁД", "Сладкий янтарный подарок трудолюбивых пчёл к чаю", "Кулинария", WordTheme.HOME_COOKING),
        WordDefinition("МЕД", "Сладкий янтарный подарок трудолюбивых пчёл к чаю", "Кулинария", WordTheme.HOME_COOKING),
        WordDefinition("ХЛЕБ", "Душистая румяная буханка с хрустящей корочкой", "Кулинария", WordTheme.HOME_COOKING),
        WordDefinition("БОРЩ", "Традиционный наваристый свекольный суп со сметаной", "Кулинария", WordTheme.HOME_COOKING),
        WordDefinition("КАША", "Полезный тёплый завтрак с маслом и ягодами для детей и взрослых", "Кулинария", WordTheme.HOME_COOKING),
        WordDefinition("ТОРТ", "Праздничное сладкое угощение со свечами на день рождения", "Кулинария", WordTheme.HOME_COOKING),
        WordDefinition("ЛИМОН", "Ярко-жёлтый ароматный цитрус, который кладут в горячий чай", "Кулинария", WordTheme.HOME_COOKING),
        WordDefinition("ЯБЛОКО", "Сочный наливной фрукт из домашнего сада", "Кулинария", WordTheme.HOME_COOKING),
        WordDefinition("ОКНО", "Светлый проём со стеклом, через который в комнату заглядывает солнце", "Дом и Уют", WordTheme.HOME_COOKING),
        WordDefinition("ЛАМПА", "Уютный настольный светильник под абажуром для вечернего чтения", "Дом и Уют", WordTheme.HOME_COOKING),
        WordDefinition("ШАРФ", "Тёплая вязаная вещь, заботливо согревающая шею зимой", "Дом и Уют", WordTheme.HOME_COOKING),
        WordDefinition("ПЛЕД", "Мягкое тёплое покрывало, в которое приятно укутаться в кресле", "Дом и Уют", WordTheme.HOME_COOKING),
        WordDefinition("КЛЮЧ", "Металлический помощник, открывающий дверь родного дома", "Дом и Уют", WordTheme.HOME_COOKING),
        WordDefinition("ЧАСЫ", "Хранители домашнего времени с тикающими стрелками", "Дом и Уют", WordTheme.HOME_COOKING),
        WordDefinition("СТОЛ", "Главное место на кухне, за которым собирается вся семья", "Дом и Уют", WordTheme.HOME_COOKING),
        WordDefinition("ВАЗА", "Красивый сосуд на столе для свежего букета цветов", "Дом и Уют", WordTheme.HOME_COOKING),

        // 🌿 Природа и Животные
        WordDefinition("КОТ", "Домашний пушистый любимец, который сладко мурлычет на коленях", "Животные", WordTheme.NATURE_ANIMALS),
        WordDefinition("ПЁС", "Самый верный четвероногий друг человека, радостно виляющий хвостом", "Животные", WordTheme.NATURE_ANIMALS),
        WordDefinition("ПЕС", "Самый верный четвероногий друг человека, радостно виляющий хвостом", "Животные", WordTheme.NATURE_ANIMALS),
        WordDefinition("ЗАЯЦ", "Ушастый лесной прыгун, который зимой меняет шубку на белую", "Животные", WordTheme.NATURE_ANIMALS),
        WordDefinition("ЛИСА", "Рыжая лесная красавица с пышным хвостом из русских сказок", "Животные", WordTheme.NATURE_ANIMALS),
        WordDefinition("БЕЛКА", "Пушистая лесная хлопотунья, собирающая орехи и шишки на зиму", "Животные", WordTheme.NATURE_ANIMALS),
        WordDefinition("СОВА", "Мудрая ночная птица с большими круглыми глазами", "Животные", WordTheme.NATURE_ANIMALS),
        WordDefinition("ТИГР", "Величественный полосатый житель уссурийской тайги", "Животные", WordTheme.NATURE_ANIMALS),
        WordDefinition("СЛОН", "Добрый великан с длинным хоботом и большими ушами", "Животные", WordTheme.NATURE_ANIMALS),
        WordDefinition("ЛЕС", "Зелёное царство деревьев, пения птиц и грибных тропинок", "Природа", WordTheme.NATURE_ANIMALS),
        WordDefinition("САД", "Цветущий уголок вокруг дома с яблонями, вишнями и клумбами", "Природа", WordTheme.NATURE_ANIMALS),
        WordDefinition("МОРЕ", "Бескрайний тёплый простор с солёными волнами и чайками", "Природа", WordTheme.NATURE_ANIMALS),
        WordDefinition("РЕКА", "Живая серебристая лента воды, текущая меж зелёных берегов", "Природа", WordTheme.NATURE_ANIMALS),
        WordDefinition("ВОДА", "Прозрачная живительная влага в роднике и ручье", "Природа", WordTheme.NATURE_ANIMALS),
        WordDefinition("ВЕТЕР", "Лёгкое дыхание воздуха, колышущее листву деревьев", "Природа", WordTheme.NATURE_ANIMALS),
        WordDefinition("СНЕГ", "Белое пушистое одеяло зимы из резных снежинок", "Природа", WordTheme.NATURE_ANIMALS),
        WordDefinition("ВОЛНА", "Морской гребень, ласково набегающий на песчаный берег", "Природа", WordTheme.NATURE_ANIMALS),
        WordDefinition("ГОРА", "Высокая каменная вершина, уходящая к самым облакам", "Природа", WordTheme.NATURE_ANIMALS),
        WordDefinition("РОЗА", "Царица цветочной клумбы с нежными лепестками и дивным ароматом", "Природа", WordTheme.NATURE_ANIMALS),
        WordDefinition("ЛУНА", "Ночное серебристое светило, укачивающее землю ко сну", "Природа", WordTheme.NATURE_ANIMALS),

        // 🧚 Сказки, Детство и Добрые Слова
        WordDefinition("МАМА", "Самый родной, нежный и заботливый человек на свете", "Семья", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("ДЕТИ", "Маленькие исследователи мира, наполняющие дом радостным смехом", "Семья", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("ДРУГ", "Тот, с кем радостно делиться игрушками, секретами и приключениями", "Семья", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("ЧУДО", "Волшебное событие из доброй сказки, в которое так приятно верить", "Сказки", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("ФЕЯ", "Добрая крылатая волшебница с волшебной палочкой из сказки", "Сказки", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("ЗАМОК", "Сказочный дворец с высокими башнями для принцев и принцесс", "Сказки", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("КЛАД", "Старинный сундук с сокровищами на таинственном острове", "Сказки", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("ГНОМ", "Маленький бородатый хранитель подземных самоцветов в колпачке", "Сказки", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("МЯЧ", "Упругий круглый спутник весёлых детских игр во дворе", "Детство", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("КУКЛА", "Любимая игрушка, которую укладывают в игрушечную кроватку", "Детство", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("ПАЗЛ", "Складная картина или слово из отдельных фигурных кусочков", "Детство", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("ИГРА", "Увлекательное занятие для отдыха, смекалки и радости", "Детство", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("КНИГА", "Верный бумажный друг со сказками, картинками и историями", "Культура", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("СЛОВО", "Сложенные вместе буквы, рождающие смысл и разгадку кроссворда", "Культура", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("БУКВА", "Один из тридцати трёх знаков нашей родной азбуки", "Культура", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("ПЕСНЯ", "Добрая мелодия со словами, которую поют вместе за праздником", "Искусство", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("МЕЧТА", "Светлое заветное желание в сердце каждого человека", "Сказки", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("УСПЕХ", "Радостная победа, когда пазл или кроссворд сложился идеально", "Детство", WordTheme.FAIRY_TALES_KIDS)
    )

    val EN_WORDS = listOf(
        // 🏡 Home & Cooking
        WordDefinition("HOME", "The warm, cozy place where family gathers in peace", "Home", WordTheme.HOME_COOKING),
        WordDefinition("CAKE", "A sweet baked dessert decorated for family celebrations", "Cooking", WordTheme.HOME_COOKING),
        WordDefinition("BREAD", "Warm golden loaf freshly baked in the kitchen oven", "Cooking", WordTheme.HOME_COOKING),
        WordDefinition("SOUP", "A comforting warm bowl of broth and vegetables", "Cooking", WordTheme.HOME_COOKING),
        WordDefinition("APPLE", "A crisp, sweet orchard fruit great for baking pies", "Cooking", WordTheme.HOME_COOKING),
        WordDefinition("LEMON", "A bright yellow citrus fruit lovely in warm tea", "Cooking", WordTheme.HOME_COOKING),
        WordDefinition("HONEY", "Sweet golden nectar made by busy bees", "Cooking", WordTheme.HOME_COOKING),
        WordDefinition("LAMP", "A gentle bedside light for cozy evening reading", "Home", WordTheme.HOME_COOKING),
        WordDefinition("CHAIR", "A comfortable seat by the kitchen table or fireplace", "Home", WordTheme.HOME_COOKING),
        WordDefinition("CLOCK", "The gentle ticking keeper of household time", "Home", WordTheme.HOME_COOKING),

        // 🌿 Nature & Animals
        WordDefinition("CAT", "A soft purring family pet curled up on a cushion", "Animals", WordTheme.NATURE_ANIMALS),
        WordDefinition("DOG", "A loyal four-legged friend always happy to see you", "Animals", WordTheme.NATURE_ANIMALS),
        WordDefinition("DUCK", "A friendly water bird that paddles across the pond", "Animals", WordTheme.NATURE_ANIMALS),
        WordDefinition("BEAR", "A big furry forest dweller who loves sweet berries and honey", "Animals", WordTheme.NATURE_ANIMALS),
        WordDefinition("BIRD", "A feathered singer perched high in the garden branches", "Animals", WordTheme.NATURE_ANIMALS),
        WordDefinition("TREE", "A tall leafy guardian offering shade in the park", "Nature", WordTheme.NATURE_ANIMALS),
        WordDefinition("ROSE", "A fragrant garden flower with velvety petals", "Nature", WordTheme.NATURE_ANIMALS),
        WordDefinition("RAIN", "Gentle drops from the clouds watering the garden flowers", "Nature", WordTheme.NATURE_ANIMALS),
        WordDefinition("SNOW", "Soft white winter flakes covering the rooftops", "Nature", WordTheme.NATURE_ANIMALS),
        WordDefinition("MOON", "The silver night lantern shining among the stars", "Nature", WordTheme.NATURE_ANIMALS),
        WordDefinition("STAR", "A twinkling point of golden light in the night sky", "Nature", WordTheme.NATURE_ANIMALS),

        // 🧚 Fairy Tales & Kids
        WordDefinition("MAMA", "The kindest, warmest person in every child's world", "Family", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("LOVE", "Warm affection that brings the whole family together", "Family", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("SMILE", "A bright happy expression that warms everyone around", "Family", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("FAIRY", "A tiny winged magical helper from bedtime stories", "Fairy Tales", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("MAGIC", "Wonderful storybook enchantment that makes wishes come true", "Fairy Tales", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("KING", "A wise royal ruler wearing a golden crown in a castle", "Fairy Tales", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("BOOK", "A treasury of bedtime stories and colorful adventures", "Kids", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("BALL", "A bouncy round toy for cheerful backyard games", "Kids", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("GAME", "A joyful puzzle shared by children, parents, and grandparents", "Kids", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("SONG", "A cheerful melody sung together with family", "Kids", WordTheme.FAIRY_TALES_KIDS),
        WordDefinition("DREAM", "A sweet nighttime story or cherished wish", "Fairy Tales", WordTheme.FAIRY_TALES_KIDS)
    )

    fun getDefinitionsForLanguage(isRussian: Boolean): List<WordDefinition> {
        return if (isRussian) RU_WORDS else EN_WORDS
    }

    fun getDefinitionsForTheme(isRussian: Boolean, theme: WordTheme): List<WordDefinition> {
        val base = getDefinitionsForLanguage(isRussian)
        if (theme == WordTheme.ALL) return base
        val filtered = base.filter { it.themeGroup == theme }
        return filtered.ifEmpty { base }
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
        return if (isRussian) "Семья" else "Family"
    }
}
