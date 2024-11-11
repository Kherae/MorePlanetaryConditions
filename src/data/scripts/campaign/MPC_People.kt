package data.scripts.campaign

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.PersonImportance
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.People
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.ids.Voices
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator
import data.utilities.niko_MPC_ids

object MPC_People {
    const val KANTA_GOON_ONE = "MPC_kanta_goon_one"
    const val KANTA_GOON_TWO = "MPC_kanta_goon_two"

    const val VIOLENCE_GUARD = "MPC_violenceGuard"
    const val INTELLIGENCE_GUARD = "MPC_intelligenceGuard"
    const val MORALITY_GUARD = "MPC_moralityGuard"

    const val PLAYER_FACTION_INTSEC_SQUAD_CHIEF = "MPC_playerFacIntsecSquadChief"
    const val HEGEMONY_SPY = "MPC_hegemonySpy"

    fun createCharacters() {
        if (Global.getSector().memoryWithoutUpdate[niko_MPC_ids.IMPORTANT_PEOPLE] == null) {
            Global.getSector().memoryWithoutUpdate[niko_MPC_ids.IMPORTANT_PEOPLE] = HashMap<String, PersonAPI>()
        }

        val importantPeople = Global.getSector().importantPeople

        // Pirate bar encounter after delivering loke, points you to the magnetar quest
        val MPC_importantPeople = Global.getSector().memoryWithoutUpdate[niko_MPC_ids.IMPORTANT_PEOPLE] as HashMap<String, PersonAPI>
        if (MPC_importantPeople[KANTA_GOON_ONE] == null) {
            val goonOne = Global.getSector().getFaction(Factions.PIRATES).createRandomPerson(StarSystemGenerator.random)
            goonOne.id = KANTA_GOON_ONE
            goonOne.rankId = Ranks.SPECIAL_AGENT
            goonOne.postId = Ranks.SPECIAL_AGENT
            goonOne.importance = PersonImportance.HIGH
            goonOne.portraitSprite = "graphics/portraits/portrait_mercenary01.png"
            goonOne.name = FullName("Jamwell", "Pourus", FullName.Gender.MALE)
            goonOne.gender = FullName.Gender.MALE
            goonOne.voice = Voices.VILLAIN
            importantPeople.addPerson(goonOne)
            MPC_importantPeople[KANTA_GOON_ONE] = goonOne
        }

        if (MPC_importantPeople[KANTA_GOON_TWO] == null) {
            val goonTwo = Global.getSector().getFaction(Factions.PIRATES).createRandomPerson(StarSystemGenerator.random)
            goonTwo.id = KANTA_GOON_TWO
            goonTwo.rankId = Ranks.SPECIAL_AGENT
            goonTwo.postId = Ranks.SPECIAL_AGENT
            goonTwo.importance = PersonImportance.HIGH
            goonTwo.portraitSprite = "graphics/portraits/portrait_pirate14.png"
            goonTwo.name = FullName("Bob", "Joe", FullName.Gender.MALE)
            goonTwo.gender = FullName.Gender.MALE
            goonTwo.voice = Voices.VILLAIN
            importantPeople.addPerson(goonTwo)
            MPC_importantPeople[KANTA_GOON_TWO] = goonTwo
        }

        if (MPC_importantPeople[VIOLENCE_GUARD] == null) {
            val violenceGuard = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson(StarSystemGenerator.random)
            violenceGuard.id = VIOLENCE_GUARD
            violenceGuard.rankId = Ranks.AGENT
            violenceGuard.postId = Ranks.AGENT
            violenceGuard.importance = PersonImportance.HIGH
            violenceGuard.portraitSprite = "graphics/portraits/MPC_violenceGuard.png"
            violenceGuard.name = FullName("Pontus", "Excalatus", FullName.Gender.MALE)
            violenceGuard.gender = FullName.Gender.MALE
            violenceGuard.voice = Voices.VILLAIN
            importantPeople.addPerson(violenceGuard)
            MPC_importantPeople[VIOLENCE_GUARD] = violenceGuard
        }

        if (MPC_importantPeople[INTELLIGENCE_GUARD] == null) {
            val intelligenceGuard = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson(StarSystemGenerator.random)
            intelligenceGuard.id = INTELLIGENCE_GUARD
            intelligenceGuard.rankId = Ranks.AGENT
            intelligenceGuard.postId = Ranks.AGENT
            intelligenceGuard.importance = PersonImportance.HIGH
            intelligenceGuard.portraitSprite = "graphics/portraits/MPC_intelligenceGuard.png"
            intelligenceGuard.name = FullName("Erasmus", "XII", FullName.Gender.MALE)
            intelligenceGuard.gender = FullName.Gender.MALE
            intelligenceGuard.voice = Voices.ARISTO
            importantPeople.addPerson(intelligenceGuard)
            MPC_importantPeople[INTELLIGENCE_GUARD] = intelligenceGuard
        }

        if (MPC_importantPeople[MORALITY_GUARD] == null) {
            val moralityGuard = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson(StarSystemGenerator.random)
            moralityGuard.id = MORALITY_GUARD
            moralityGuard.rankId = Ranks.AGENT
            moralityGuard.postId = Ranks.AGENT
            moralityGuard.importance = PersonImportance.HIGH
            moralityGuard.portraitSprite = "graphics/portraits/MPC_moralityGuard.png"
            moralityGuard.name = FullName("Simon", "Geria", FullName.Gender.MALE)
            moralityGuard.gender = FullName.Gender.MALE
            moralityGuard.voice = Voices.FAITHFUL
            importantPeople.addPerson(moralityGuard)
            MPC_importantPeople[MORALITY_GUARD] = moralityGuard
        }

        if (MPC_importantPeople[PLAYER_FACTION_INTSEC_SQUAD_CHIEF] == null) {
            val intsecChief = Global.getSector().getFaction(Factions.PLAYER).createRandomPerson(StarSystemGenerator.random)
            intsecChief.id = PLAYER_FACTION_INTSEC_SQUAD_CHIEF
            intsecChief.rankId = Ranks.SPECIAL_AGENT
            intsecChief.postId = Ranks.SPECIAL_AGENT
            intsecChief.importance = PersonImportance.VERY_HIGH
            //moralityGuard.portraitSprite = "graphics/portraits/MPC_moralityGuard.png"
            //moralityGuard.name = FullName("Simon", "Geria", FullName.Gender.MALE)
            //moralityGuard.gender = FullName.Gender.MALE
            //moralityGuard.voice = Voices.FAITHFUL
            importantPeople.addPerson(intsecChief)
            MPC_importantPeople[PLAYER_FACTION_INTSEC_SQUAD_CHIEF] = intsecChief
        }

        if (MPC_importantPeople[HEGEMONY_SPY] == null) {
            val hegemonySpy = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson()
            hegemonySpy.id = HEGEMONY_SPY
            hegemonySpy.rankId = Ranks.CITIZEN
            hegemonySpy.postId = Ranks.CITIZEN
            hegemonySpy.importance = PersonImportance.MEDIUM
            hegemonySpy.portraitSprite = "graphics/portraits/portrait23.png"
            hegemonySpy.name = FullName("Bodewell", "Calus", FullName.Gender.MALE)
            hegemonySpy.gender = FullName.Gender.MALE
            //moralityGuard.voice = Voices.FAITHFUL
            importantPeople.addPerson(hegemonySpy)
            MPC_importantPeople[HEGEMONY_SPY] = hegemonySpy
        }
        Global.getSector().memoryWithoutUpdate[niko_MPC_ids.GENERATED_PEOPLE] = true
    }
}