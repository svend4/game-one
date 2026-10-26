package com.example.game

import java.util.Calendar

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
    val themeGroup: WordTheme = WordTheme.ALL,
    val funFact: String = "",  // Educational fun fact for the Family Encyclopedia Album
    val isCustomFamilyWord: Boolean = false
)

object Dictionary {
    const val WILDCARD_CHAR = '★'

    val customFamilyWords = mutableListOf<WordDefinition>()

    val RU_WORDS = listOf(
        // 🏡 Уютный Дом и Кулинария (Для мам, бабушек и всей семьи)
        WordDefinition(
            "ДОМ",
            "Уютное место, где живёт дружная семья и всегда тепло",
            "Дом и Уют",
            WordTheme.HOME_COOKING,
            "В старину на Руси дом строили всей деревней за один день — такой обычай назывался «помочь», а на крышу ставили резного конька-оберега."
        ),
        WordDefinition(
            "ЧАЙ",
            "Душистый согревающий напиток для вечерней беседы всей семьёй",
            "Кулинария",
            WordTheme.HOME_COOKING,
            "Традиция семейного чаепития за самоваром объединяла поколения: к чаю подавали мёд, баранки и домашнее варенье из смородины."
        ),
        WordDefinition(
            "ПИРОГ",
            "Румяная домашняя выпечка с яблоками, ягодами или капустой",
            "Кулинария",
            WordTheme.HOME_COOKING,
            "Слово «пирог» происходит от древнерусского слова «пир» — праздничный хлеб, который пекли только для самых дорогих гостей."
        ),
        WordDefinition(
            "БЛИН",
            "Круглое золотистое угощение со сковороды, похожее на солнышко",
            "Кулинария",
            WordTheme.HOME_COOKING,
            "Румяный круглый блин издавна символизирует весеннее тёплое солнце и пробуждение природы."
        ),
        WordDefinition(
            "МЕД",
            "Сладкий янтарный подарок трудолюбивых пчёл к чаю",
            "Кулинария",
            WordTheme.HOME_COOKING,
            "Чтобы собрать всего одну баночку цветочного мёда, пчёлам нужно облететь несколько миллионов лесных и луговых цветов!"
        ),
        WordDefinition(
            "ХЛЕБ",
            "Душистая румяная буханка с хрустящей корочкой",
            "Кулинария",
            WordTheme.HOME_COOKING,
            "Аромат свежеиспечённого домашнего хлеба рождается при подрумянивании корочки в печи и сразу создаёт в доме ощущение тепла и уюта."
        ),
        WordDefinition(
            "БОРЩ",
            "Традиционный наваристый свекольный суп со сметаной",
            "Кулинария",
            WordTheme.HOME_COOKING,
            "В каждой семье есть свой секрет вкусного борща — в старину его томили в глиняном горшке в русской печи несколько часов."
        ),
        WordDefinition(
            "КАША",
            "Полезный тёплый завтрак с маслом и ягодами для детей и взрослых",
            "Кулинария",
            WordTheme.HOME_COOKING,
            "Пословица «Щи да каша — пища наша» напоминает о том, что злаки дарят бодрость и силы на весь день."
        ),
        WordDefinition(
            "ТОРТ",
            "Праздничное сладкое угощение со свечами на день рождения",
            "Кулинария",
            WordTheme.HOME_COOKING,
            "Обычай задувать свечи на праздничном торте и загадывать заветное желание пришёл к нам из глубокой древности."
        ),
        WordDefinition(
            "ЛИМОН",
            "Ярко-жёлтый ароматный цитрус, который кладут в горячий чай",
            "Кулинария",
            WordTheme.HOME_COOKING,
            "Лимонное дерево может цвести и плодоносить круглый год, наполняя комнату тонким освежающим ароматом."
        ),
        WordDefinition(
            "ЯБЛОКО",
            "Сочный наливной фрукт из домашнего сада",
            "Кулинария",
            WordTheme.HOME_COOKING,
            "В русских народных сказках наливное яблочко на серебряном блюдечке показывало далёкие края и чудеса."
        ),
        WordDefinition(
            "ОКНО",
            "Светлый проём со стеклом, через который в комнату заглядывает солнце",
            "Дом и Уют",
            WordTheme.HOME_COOKING,
            "Слово «окно» родственно слову «око» (глаз) — ведь через него дом смотрит на окружающий мир и впускает солнечный свет."
        ),
        WordDefinition(
            "ЛАМПА",
            "Уютный настольный светильник под абажуром для вечернего чтения",
            "Дом и Уют",
            WordTheme.HOME_COOKING,
            "Мягкий тёплый свет настольной лампы с тканевым абажуром создаёт в комнате атмосферу покоя и вечерней сказки."
        ),
        WordDefinition(
            "ШАРФ",
            "Тёплая вязаная вещь, заботливо согревающая шею зимой",
            "Дом и Уют",
            WordTheme.HOME_COOKING,
            "Связанный вручную бабушкин шарф хранит тепло добрых рук и защищает от самого студёного зимнего ветра."
        ),
        WordDefinition(
            "ПЛЕД",
            "Мягкое тёплое покрывало, в которое приятно укутаться в кресле",
            "Дом и Уют",
            WordTheme.HOME_COOKING,
            "Клетчатые шерстяные пледы появились в горной Шотландии, где у каждой семьи был свой уникальный цветной узор."
        ),
        WordDefinition(
            "КЛЮЧ",
            "Металлический помощник, открывающий дверь родного дома",
            "Дом и Уют",
            WordTheme.HOME_COOKING,
            "В русском языке слово «ключ» означает и дверной ключик, и чистый лесной родник, и разгадку к интересной тайне!"
        ),
        WordDefinition(
            "ЧАСЫ",
            "Хранители домашнего времени с тикающими стрелками",
            "Дом и Уют",
            WordTheme.HOME_COOKING,
            "Стрелки часов движутся «по часовой стрелке» потому, что именно в эту сторону бежала тень на древних солнечных часах."
        ),
        WordDefinition(
            "СТОЛ",
            "Главное место на кухне, за которым собирается вся семья",
            "Дом и Уют",
            WordTheme.HOME_COOKING,
            "От слова «стол» произошло слово «столица» и «хлебосольное застолье», объединяющее близких людей."
        ),
        WordDefinition(
            "ВАЗА",
            "Красивый сосуд на столе для свежего букета цветов",
            "Дом и Уют",
            WordTheme.HOME_COOKING,
            "Если в воду в цветочной вазе добавить щепотку сахара, живые цветы простоят свежими намного дольше!"
        ),

        // 🌿 Природа и Животные
        WordDefinition(
            "КОТ",
            "Домашний пушистый любимец, который сладко мурлычет на коленях",
            "Животные",
            WordTheme.NATURE_ANIMALS,
            "Мурлыканье домашнего кота звучит на частоте, которая помогает человеку успокоиться и снять усталость."
        ),
        WordDefinition(
            "ПЕС",
            "Самый верный четвероногий друг человека, радостно виляющий хвостом",
            "Животные",
            WordTheme.NATURE_ANIMALS,
            "Собаки умеют понимать до 200 человеческих слов и безошибочно чувствуют настроение своих хозяев!"
        ),
        WordDefinition(
            "ЗАЯЦ",
            "Ушастый лесной прыгун, который зимой меняет шубку на белую",
            "Животные",
            WordTheme.NATURE_ANIMALS,
            "Длинные уши помогают зайцу не только чутко слышать шорохи леса, но и охлаждаться в жаркую летнюю погоду."
        ),
        WordDefinition(
            "ЛИСА",
            "Рыжая лесная красавица с пышным хвостом из русских сказок",
            "Животные",
            WordTheme.NATURE_ANIMALS,
            "Зимой пушистый хвост служит лисичке тёплым одеялом: сворачиваясь калачиком, она укрывает им свой нос и лапки."
        ),
        WordDefinition(
            "БЕЛКА",
            "Пушистая лесная хлопотунья, собирающая орехи и шишки на зиму",
            "Животные",
            WordTheme.NATURE_ANIMALS,
            "Пряча жёлуди и орешки про запас в землю, белочки невольно сажают новые лесные деревья!"
        ),
        WordDefinition(
            "СОВА",
            "Мудрая ночная птица с большими круглыми глазами",
            "Животные",
            WordTheme.NATURE_ANIMALS,
            "Маховые перья совы устроены так мягко, что её полёт в ночном лесу совершенно бесшумен."
        ),
        WordDefinition(
            "ТИГР",
            "Величественный полосатый житель уссурийской тайги",
            "Животные",
            WordTheme.NATURE_ANIMALS,
            "У каждого тигра рисунок полосок на шерсти неповторим — в природе нет двух одинаковых полосатых узоров."
        ),
        WordDefinition(
            "СЛОН",
            "Добрый великан с длинным хоботом и большими ушами",
            "Животные",
            WordTheme.NATURE_ANIMALS,
            "Слоны живут дружными семьями во главе с мудрой бабушкой-слонихой и всегда заботятся о малышах."
        ),
        WordDefinition(
            "ЛЕС",
            "Зелёное царство деревьев, пения птиц и грибных тропинок",
            "Природа",
            WordTheme.NATURE_ANIMALS,
            "Деревья в лесу общаются и поддерживают друг друга через подземную сеть корней и лесных грибов."
        ),
        WordDefinition(
            "САД",
            "Цветущий уголок вокруг дома с яблонями, вишнями и клумбами",
            "Природа",
            WordTheme.NATURE_ANIMALS,
            "Весеннее цветение яблоневого и вишнёвого сада наполняет воздух ароматом, который привлекает добрых пчёл и бабочек."
        ),
        WordDefinition(
            "МОРЕ",
            "Бескрайний тёплый простор с солёными волнами и чайками",
            "Природа",
            WordTheme.NATURE_ANIMALS,
            "Морской бриз насыщен полезными минералами, а мерный шум прибоя напоминает ритм спокойного дыхания."
        ),
        WordDefinition(
            "РЕКА",
            "Живая серебристая лента воды, текущая меж зелёных берегов",
            "Природа",
            WordTheme.NATURE_ANIMALS,
            "Издавна города и деревни строили на берегах рек, которые дарили прохладу и соединяли далёкие земли."
        ),
        WordDefinition(
            "ВОДА",
            "Прозрачная живительная влага в роднике и ручье",
            "Природа",
            WordTheme.NATURE_ANIMALS,
            "Вода — единственное вещество на нашей планете, которое мы встречаем в природе сразу в трёх видах: ручей, облако и снежинка."
        ),
        WordDefinition(
            "ВЕТЕР",
            "Лёгкое дыхание воздуха, колышущее листву деревьев",
            "Природа",
            WordTheme.NATURE_ANIMALS,
            "Ласковый летний ветерок переносит крылатые семена цветов и одуванчиков на новые солнечные полянки."
        ),
        WordDefinition(
            "СНЕГ",
            "Белое пушистое одеяло зимы из резных снежинок",
            "Природа",
            WordTheme.NATURE_ANIMALS,
            "Кристаллы льда в облаках обычно растут в виде шестилучевых звёздочек, причём кружевной узор каждой снежинки складывается неповторимо!"
        ),
        WordDefinition(
            "ВОЛНА",
            "Морской гребень, ласково набегающий на песчаный берег",
            "Природа",
            WordTheme.NATURE_ANIMALS,
            "Морские волны за века обтачивают острые камешки на берегу, превращая их в гладкую разноцветную гальку."
        ),
        WordDefinition(
            "ГОРА",
            "Высокая каменная вершина, уходящая к самым облакам",
            "Природа",
            WordTheme.NATURE_ANIMALS,
            "На склонах одной большой горы можно за один день увидеть все времена года — от цветущего лета внизу до снежной зимы на вершине."
        ),
        WordDefinition(
            "РОЗА",
            "Царица цветочной клумбы с нежными лепестками и дивным ароматом",
            "Природа",
            WordTheme.NATURE_ANIMALS,
            "Садовые розы выращивают уже более пяти тысяч лет, а из их лепестков готовят душистое варенье и розовую воду."
        ),
        WordDefinition(
            "ЛУНА",
            "Ночное серебристое светило, укачивающее землю ко сну",
            "Природа",
            WordTheme.NATURE_ANIMALS,
            "Луна всегда повёрнута к Земле одной и той же стороной и управляет морскими приливами и отливами."
        ),

        // 🧚 Сказки, Детство и Добрые Слова
        WordDefinition(
            "МАМА",
            "Самый родной, нежный и заботливый человек на свете",
            "Семья",
            WordTheme.FAIRY_TALES_KIDS,
            "Почти на всех языках мира слово «мама» звучит одинаково нежно, ведь это первые слоги, которые произносит малыш."
        ),
        WordDefinition(
            "ДЕТИ",
            "Маленькие исследователи мира, наполняющие дом радостным смехом",
            "Семья",
            WordTheme.FAIRY_TALES_KIDS,
            "Любознательные дети задают до трёхсот вопросов в день, каждый раз открывая для себя и взрослых чудеса окружающего мира!"
        ),
        WordDefinition(
            "ДРУГ",
            "Тот, с кем радостно делиться игрушками, секретами и приключениями",
            "Семья",
            WordTheme.FAIRY_TALES_KIDS,
            "Слово «друг» в древности означало «близкий спутник, который держит тебя за руку в пути»."
        ),
        WordDefinition(
            "ЧУДО",
            "Волшебное событие из доброй сказки, в которое так приятно верить",
            "Сказки",
            WordTheme.FAIRY_TALES_KIDS,
            "Самые настоящие чудеса рождаются из доброты, внимания к близким и искренней улыбки."
        ),
        WordDefinition(
            "ФЕЯ",
            "Добрая крылатая волшебница с волшебной палочкой из сказки",
            "Сказки",
            WordTheme.FAIRY_TALES_KIDS,
            "В европейских сказках цветочные феи ночуют в бутонах колокольчиков и зажигают утреннюю росу на траве."
        ),
        WordDefinition(
            "ЗАМОК",
            "Сказочный дворец с высокими башнями для принцев и принцесс",
            "Сказки",
            WordTheme.FAIRY_TALES_KIDS,
            "В зависимости от ударения слово «замок» превращается из рыцарского дворца (за́мок) в надёжного стража дверей (замо́к)!"
        ),
        WordDefinition(
            "КЛАД",
            "Старинный сундук с сокровищами на таинственном острове",
            "Сказки",
            WordTheme.FAIRY_TALES_KIDS,
            "В сказках говорится, что главный клад — это не золотые монеты, а знания, смекалка и дружная семья."
        ),
        WordDefinition(
            "ГНОМ",
            "Маленький бородатый хранитель подземных самоцветов в колпачке",
            "Сказки",
            WordTheme.FAIRY_TALES_KIDS,
            "Сказочные садовые гномики считаются добрыми помощниками, которые по ночам присматривают за цветами и грядками."
        ),
        WordDefinition(
            "МЯЧ",
            "Упругий круглый спутник весёлых детских игр во дворе",
            "Детство",
            WordTheme.FAIRY_TALES_KIDS,
            "Игры с мячом развивают ловкость и реакцию, а стихотворение про Таню и мячик знает наизусть каждый ребёнок!"
        ),
        WordDefinition(
            "КУКЛА",
            "Любимая игрушка, которую укладывают в игрушечную кроватку",
            "Детство",
            WordTheme.FAIRY_TALES_KIDS,
            "Деревянная расписная матрёшка — одна из самых известных семейных кукол в мире, символизирующая материнскую заботу."
        ),
        WordDefinition(
            "ПАЗЛ",
            "Складная картина или слово из отдельных фигурных кусочков",
            "Детство",
            WordTheme.FAIRY_TALES_KIDS,
            "Первые в мире пазлы были придуманы более двухсот лет назад как увлекательное учебное пособие по географии для детей!"
        ),
        WordDefinition(
            "ИГРА",
            "Увлекательное занятие для отдыха, смекалки и радости",
            "Детство",
            WordTheme.FAIRY_TALES_KIDS,
            "Совместные настольные и словесные игры тренируют память у пожилых людей и расширяют словарный запас у детей."
        ),
        WordDefinition(
            "КНИГА",
            "Верный бумажный друг со сказками, картинками и историями",
            "Культура",
            WordTheme.FAIRY_TALES_KIDS,
            "Семейное чтение книги вслух перед сном — одна из самых тёплых традиций, развивающая воображение ребёнка."
        ),
        WordDefinition(
            "СЛОВО",
            "Сложенные вместе буквы, рождающие смысл и разгадку кроссворда",
            "Культура",
            WordTheme.FAIRY_TALES_KIDS,
            "Доброе слово способно поднять настроение на весь день и согреть сердце даже на расстоянии."
        ),
        WordDefinition(
            "БУКВА",
            "Один из тридцати трёх знаков нашей родной азбуки",
            "Культура",
            WordTheme.FAIRY_TALES_KIDS,
            "Самая молодая буква русского алфавита — буква «Ё», в её честь даже установлен настоящий памятник!"
        ),
        WordDefinition(
            "ПЕСНЯ",
            "Добрая мелодия со словами, которую поют вместе за праздником",
            "Искусство",
            WordTheme.FAIRY_TALES_KIDS,
            "Колыбельная песня мамы или бабушки — самая первая музыка, которую слышит человек в своей жизни."
        ),
        WordDefinition(
            "МЕЧТА",
            "Светлое заветное желание в сердце каждого человека",
            "Сказки",
            WordTheme.FAIRY_TALES_KIDS,
            "Мечты вдохновляют первооткрывателей, художников и учёных создавать удивительные изобретения."
        ),
        WordDefinition(
            "УСПЕХ",
            "Радостная победа, когда пазл или кроссворд сложился идеально",
            "Детство",
            WordTheme.FAIRY_TALES_KIDS,
            "Каждый разгаданный кроссворд — это маленький праздник для ума и отличная зарядка для памяти!"
        )
    )

    val EN_WORDS = listOf(
        // 🏡 Home & Cooking
        WordDefinition(
            "HOME",
            "The warm, cozy place where family gathers in peace",
            "Home",
            WordTheme.HOME_COOKING,
            "The proverb 'Home is where the heart is' has been cherished by families around the world for centuries."
        ),
        WordDefinition(
            "CAKE",
            "A sweet baked dessert decorated for family celebrations",
            "Cooking",
            WordTheme.HOME_COOKING,
            "Baking a family cake together is a wonderful way for kids to learn measuring, patience, and creativity."
        ),
        WordDefinition(
            "BREAD",
            "Warm golden loaf freshly baked in the kitchen oven",
            "Cooking",
            WordTheme.HOME_COOKING,
            "Sharing warm bread at the table is one of humanity's oldest symbols of friendship and hospitality."
        ),
        WordDefinition(
            "SOUP",
            "A comforting warm bowl of broth and vegetables",
            "Cooking",
            WordTheme.HOME_COOKING,
            "Warm homemade soup has been a favorite family comfort recipe across every culture for thousands of years."
        ),
        WordDefinition(
            "APPLE",
            "A crisp, sweet orchard fruit great for baking pies",
            "Cooking",
            WordTheme.HOME_COOKING,
            "Apples float in water because a quarter of their volume is actually fresh air!"
        ),
        WordDefinition(
            "LEMON",
            "A bright yellow citrus fruit lovely in warm tea",
            "Cooking",
            WordTheme.HOME_COOKING,
            "A single lemon tree can produce up to 600 pounds of fragrant yellow lemons in a single year."
        ),
        WordDefinition(
            "HONEY",
            "Sweet golden nectar made by busy bees",
            "Cooking",
            WordTheme.HOME_COOKING,
            "Honeybees communicate the location of blooming flower meadows by performing a joyful 'waggle dance'!"
        ),
        WordDefinition(
            "LAMP",
            "A gentle bedside light for cozy evening reading",
            "Home",
            WordTheme.HOME_COOKING,
            "Warm amber reading lamps help relax the eyes before bedtime and make story time extra cozy."
        ),
        WordDefinition(
            "CHAIR",
            "A comfortable seat by the kitchen table or fireplace",
            "Home",
            WordTheme.HOME_COOKING,
            "A rocking chair's gentle motion is soothing for both grandparents reading a crossword and sleepy toddlers."
        ),
        WordDefinition(
            "CLOCK",
            "The gentle ticking keeper of household time",
            "Home",
            WordTheme.HOME_COOKING,
            "Grandfather clocks were designed with long pendulums that swing once every second with calming precision."
        ),

        // 🌿 Nature & Animals
        WordDefinition(
            "CAT",
            "A soft purring family pet curled up on a cushion",
            "Animals",
            WordTheme.NATURE_ANIMALS,
            "A cat's gentle purr vibrates at a soothing frequency that helps calm nerves and bring warmth."
        ),
        WordDefinition(
            "DOG",
            "A loyal four-legged friend always happy to see you",
            "Animals",
            WordTheme.NATURE_ANIMALS,
            "Dogs have been loyal companions to human families for over 15,000 years!"
        ),
        WordDefinition(
            "DUCK",
            "A friendly water bird that paddles across the pond",
            "Animals",
            WordTheme.NATURE_ANIMALS,
            "Duck feathers are naturally waterproof, keeping ducklings warm and dry even in cool pond water."
        ),
        WordDefinition(
            "BEAR",
            "A big furry forest dweller who loves sweet berries and honey",
            "Animals",
            WordTheme.NATURE_ANIMALS,
            "Teddy bears were named in 1902 and quickly became the most beloved bedtime toy for children everywhere."
        ),
        WordDefinition(
            "BIRD",
            "A feathered singer perched high in the garden branches",
            "Animals",
            WordTheme.NATURE_ANIMALS,
            "Listening to morning birdsong in the garden is proven to boost happiness and mental clarity."
        ),
        WordDefinition(
            "TREE",
            "A tall leafy guardian offering shade in the park",
            "Nature",
            WordTheme.NATURE_ANIMALS,
            "One mature leafy tree produces enough fresh oxygen each season for a family of four!"
        ),
        WordDefinition(
            "ROSE",
            "A fragrant garden flower with velvety petals",
            "Nature",
            WordTheme.NATURE_ANIMALS,
            "Roses belong to the same botanical family as apples, pears, cherries, and strawberries!"
        ),
        WordDefinition(
            "RAIN",
            "Gentle drops from the clouds watering the garden flowers",
            "Nature",
            WordTheme.NATURE_ANIMALS,
            "The pleasant earthy scent after a gentle summer rain is called 'petrichor'."
        ),
        WordDefinition(
            "SNOW",
            "Soft white winter flakes covering the rooftops",
            "Nature",
            WordTheme.NATURE_ANIMALS,
            "Freshly fallen snow absorbs sound waves, which is why winter mornings feel so peaceful and quiet."
        ),
        WordDefinition(
            "MOON",
            "The silver night lantern shining among the stars",
            "Nature",
            WordTheme.NATURE_ANIMALS,
            "The Moon shines by reflecting sunlight and guides gentle ocean tides across the Earth."
        ),
        WordDefinition(
            "STAR",
            "A twinkling point of golden light in the night sky",
            "Nature",
            WordTheme.NATURE_ANIMALS,
            "Stars appear to twinkle because their light travels through moving layers of Earth's atmosphere."
        ),

        // 🧚 Fairy Tales & Kids
        WordDefinition(
            "MAMA",
            "The kindest, warmest person in every child's world",
            "Family",
            WordTheme.FAIRY_TALES_KIDS,
            "Across almost every human language, 'Mama' starts with the soft 'M' sound—the easiest sound for babies to make."
        ),
        WordDefinition(
            "LOVE",
            "Warm affection that brings the whole family together",
            "Family",
            WordTheme.FAIRY_TALES_KIDS,
            "A warm family hug releases oxytocin, brightening everyone's mood in just seconds."
        ),
        WordDefinition(
            "SMILE",
            "A bright happy expression that warms everyone around",
            "Family",
            WordTheme.FAIRY_TALES_KIDS,
            "Smiling is contagious—seeing a loved one smile naturally lights up happiness centers in our brain."
        ),
        WordDefinition(
            "FAIRY",
            "A tiny winged magical helper from bedtime stories",
            "Fairy Tales",
            WordTheme.FAIRY_TALES_KIDS,
            "Fairy tales have been passed down by grandparents around cozy hearths for hundreds of generations."
        ),
        WordDefinition(
            "MAGIC",
            "Wonderful storybook enchantment that makes wishes come true",
            "Fairy Tales",
            WordTheme.FAIRY_TALES_KIDS,
            "The truest magic in any story is kindness, curiosity, and helping one another."
        ),
        WordDefinition(
            "KING",
            "A wise royal ruler wearing a golden crown in a castle",
            "Fairy Tales",
            WordTheme.FAIRY_TALES_KIDS,
            "In chess and fairy tales alike, wise rulers protect their entire kingdom with patience and care."
        ),
        WordDefinition(
            "BOOK",
            "A treasury of bedtime stories and colorful adventures",
            "Kids",
            WordTheme.FAIRY_TALES_KIDS,
            "Reading stories together for just 15 minutes a day builds lifelong imagination and vocabulary."
        ),
        WordDefinition(
            "BALL",
            "A bouncy round toy for cheerful backyard games",
            "Kids",
            WordTheme.FAIRY_TALES_KIDS,
            "Playing catch with a ball is one of the oldest and happiest games shared by parents and kids."
        ),
        WordDefinition(
            "GAME",
            "A joyful puzzle shared by children, parents, and grandparents",
            "Kids",
            WordTheme.FAIRY_TALES_KIDS,
            "Word puzzle games keep the mind sharp, calm, and joyful at every age from 5 to 95!"
        ),
        WordDefinition(
            "SONG",
            "A cheerful melody sung together with family",
            "Kids",
            WordTheme.FAIRY_TALES_KIDS,
            "Singing a favorite song together helps children remember new words effortlessly."
        ),
        WordDefinition(
            "DREAM",
            "A sweet nighttime story or cherished wish",
            "Fairy Tales",
            WordTheme.FAIRY_TALES_KIDS,
            "Many great inventions and stories first appeared to their creators in a peaceful dream!"
        )
    )

    fun isValidAlphabetForLanguage(word: String, isRussian: Boolean): Boolean {
        val clean = word.uppercase().trim()
        if (clean.isEmpty()) return false
        return if (isRussian) {
            clean.all { it in 'А'..'Я' || it == 'Ё' }
        } else {
            clean.all { it in 'A'..'Z' }
        }
    }

    fun addCustomFamilyWord(word: String, clue: String, funFact: String, isRussian: Boolean): WordDefinition? {
        val cleanWord = word.uppercase().trim().filter { it.isLetter() }
        if (cleanWord.length !in 3..8 || clue.isBlank()) return null
        if (!isValidAlphabetForLanguage(cleanWord, isRussian)) return null
        val customDef = WordDefinition(
            word = cleanWord,
            definition = clue.trim(),
            category = if (isRussian) "Семейное ❤️" else "Family ❤️",
            themeGroup = WordTheme.ALL,
            funFact = funFact.trim().ifBlank {
                if (isRussian) "Особое семейное слово, заботливо загаданное для близких!"
                else "A special family word lovingly created for loved ones!"
            },
            isCustomFamilyWord = true
        )
        customFamilyWords.removeAll { normalizeYo(it.word) == normalizeYo(cleanWord) }
        customFamilyWords.add(0, customDef)
        return customDef
    }

    fun removeCustomFamilyWord(word: String): Boolean {
        val clean = normalizeYo(word)
        return customFamilyWords.removeAll { normalizeYo(it.word) == clean }
    }

    fun getCurrentCalendarDayKey(): Int {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
    }

    fun russianLettersPlural(count: Int): String {
        val mod100 = count % 100
        val mod10 = count % 10
        return when {
            mod100 in 11..14 -> "$count букв"
            mod10 == 1 -> "$count буква"
            mod10 in 2..4 -> "$count буквы"
            else -> "$count букв"
        }
    }

    fun getDefinitionsForLanguage(isRussian: Boolean): List<WordDefinition> {
        val base = if (isRussian) RU_WORDS else EN_WORDS
        val customForLang = customFamilyWords.filter { def ->
            val hasCyrillic = def.word.any { it in 'А'..'Я' || it == 'Ё' }
            if (isRussian) hasCyrillic else !hasCyrillic
        }
        return customForLang + base
    }

    fun getDefinitionsForTheme(isRussian: Boolean, theme: WordTheme): List<WordDefinition> {
        val base = getDefinitionsForLanguage(isRussian)
        if (theme == WordTheme.ALL) return base
        val filtered = base.filter { it.themeGroup == theme || it.isCustomFamilyWord }
        return filtered.ifEmpty { base }
    }

    /**
     * Returns the special Daily Word of the Day ("☀️ Слово Дня") based on calendar day
     */
    fun getDailyWord(isRussian: Boolean): WordDefinition {
        val base = if (isRussian) RU_WORDS else EN_WORDS
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return base[dayOfYear % base.size]
    }

    /**
     * Returns a warm family wish of the day
     */
    fun getDailyWish(isRussian: Boolean): String {
        val ruWishes = listOf(
            "Пусть сегодня в вашем доме звучит добрый смех, а за общим столом будет тепло и уютно!",
            "Желаем вашей семье крепкого здоровья, ясного настроения и маленьких чудес в каждом часе!",
            "Пусть сегодняшнее чаепитие соберёт вместе самых дорогих людей и подарит душевный покой!",
            "Пусть каждая разгаданная буква приносит радость детям, улыбку маме и бодрость старшему поколению!"
        )
        val enWishes = listOf(
            "May your home be filled with warm laughter, peace, and cozy family moments today!",
            "Wishing your family wonderful health, bright smiles, and little wonders all day long!",
            "May a warm cup of tea and a good puzzle bring your favorite people closer together today!",
            "May every solved word bring joy to the kids, peace to mom, and cheer to grandparents!"
        )
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val list = if (isRussian) ruWishes else enWishes
        return list[dayOfYear % list.size]
    }

    /**
     * Checks if a candidate string (which may contain '★' wildcard characters) matches a valid word.
     */
    fun isValidWord(word: String, isRussian: Boolean): Boolean {
        return findFirstMatchingDefinition(word, isRussian) != null
    }

    fun normalizeYo(input: String): String {
        return input.uppercase().trim().replace('Ё', 'Е')
    }

    /**
     * Finds the first matching WordDefinition for a string that may contain '★' wildcards.
     */
    fun findFirstMatchingDefinition(pattern: String, isRussian: Boolean): WordDefinition? {
        val clean = normalizeYo(pattern)
        if (clean.length < 3) return null
        val list = getDefinitionsForLanguage(isRussian)
        if (!clean.contains(WILDCARD_CHAR)) {
            return list.firstOrNull { normalizeYo(it.word) == clean }
        }
        return list.firstOrNull { def ->
            val defWord = normalizeYo(def.word)
            defWord.length == clean.length && defWord.indices.all { idx ->
                clean[idx] == WILDCARD_CHAR || clean[idx] == defWord[idx]
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
     * Retrieves educational fun fact for a word.
     */
    fun getFunFact(word: String, isRussian: Boolean): String {
        val def = findFirstMatchingDefinition(word, isRussian)
        if (def != null && def.funFact.isNotBlank()) return def.funFact
        return if (isRussian) {
            "Это доброе слово входит в золотую коллекцию семейного словаря TetriCross!"
        } else {
            "This warm word is part of the TetriCross Family Encyclopedia collection!"
        }
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
