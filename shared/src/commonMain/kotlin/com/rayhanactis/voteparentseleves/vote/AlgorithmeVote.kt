package com.rayhanactis.voteparentseleves.vote

import com.rayhanactis.voteparentseleves.model.Bulletin
import com.rayhanactis.voteparentseleves.model.ResultatScrutin

interface AlgorithmeVote {
    fun calculerResultats(bulletins: List<Bulletin>, nbSieges: Int): ResultatScrutin
}
