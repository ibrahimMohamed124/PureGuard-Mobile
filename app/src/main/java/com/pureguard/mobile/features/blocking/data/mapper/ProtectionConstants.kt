package com.pureguard.mobile.features.blocking.data.mapper

object ProtectionConstants {
    val nsfwKeywords = listOf(
        "porn", "porno", "pornography", "xxx", "sex", "sexy", "nude", "nudes", "naked",
        "topless", "hentai", "ecchi", "ahegao", "doujin", "doujinshi", "futanari", "yiff",
        "r34", "rule34", "hardcore", "softcore", "milf", "dilf", "gilf", "jav", "anal",
        "oral", "blowjob", "handjob", "footjob", "titjob", "cumshot", "creampie", "facial",
        "deepthroat", "gangbang", "bukkake", "threesome", "foursome", "orgy", "swinger",
        "fetish", "bdsm", "bondage", "kinky", "kink", "domination", "submission", "pegging",
        "camgirl", "camboy", "camgirls", "camshow", "webcam", "escort", "escorts", "callgirl",
        "adult", "erotic", "erotica", "lewd", "lust", "orgasm", "masturbat", "fap", "jerk",
        "onlyfans", "fansly", "stripchat", "chaturbate", "cam4", "bongacams", "livejasmin",
        "fuck", "fucked", "fucking", "cock", "dick", "penis", "pussy", "vagina", "clit",
        "tits", "boobs", "nipples", "areola", "ass", "butt", "booty", "thong", "upskirt",
        "downblouse", "bbw", "ssbbw", "incest", "stepmom", "stepsis", "stepbro", "taboo",
        "whore", "slut", "bimbo", "cuckold", "hotwife", "cheating", "shemale", "tranny",
        "ladyboy", "sissy", "crossdress", "pornhub", "xvideos", "xhamster", "xnxx",
        "redtube", "youporn", "spankbang", "brazzers", "naughtyamerica", "realitykings",
        "bangbros", "digitalplayground", "nhentai", "hanime", "fakku", "hitomi", "gelbooru",
        "danbooru", "sankaku", "deepfake", "mrdeepfakes", "celebjihad", "thefappening",
        "fappening", "leaked-nudes"
    )

    val strictTlds = listOf(".xxx", ".porn", ".adult", ".sexy", ".webcam")

    val trustedDomains = setOf(
        "google.com", "gmail.com", "youtube.com", "wikipedia.org", "github.com",
        "stackoverflow.com", "mozilla.org", "apple.com", "microsoft.com", "amazon.com",
        "openai.com", "anthropic.com", "cloudflare.com"
    )

    val skipHosts = setOf(
        "youtube.com", "youtu.be", "youtube-nocookie.com",
        "google.com", "gmail.com", "accounts.google.com",
        "github.com", "stackoverflow.com", "wikipedia.org",
        "openai.com", "anthropic.com", "microsoft.com", "apple.com", "cloudflare.com"
    )
}