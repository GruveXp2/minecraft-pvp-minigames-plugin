package gruvexp.bbminigames.model.stat

import org.bukkit.Location

class AbilityTab(tabName: String, loc: Location, layoutY: Float, onExpandToggle: () -> Unit) : StatTab(tabName, loc, layoutY, onExpandToggle) {
    override val layoutWidth: Float
        get() {

        }

    val cells

    override fun expand() {
        TODO("Not yet implemented")
    }

    override fun collapse() {
        TODO("Not yet implemented")
    }

    class AbilityCell(parent: StatElement, layoutX: Float, layoutY: Float, playerStats: PlayerMatchStats) : StatElement(parent, layoutX, layoutY) {



        override fun initSelf() {
            TODO("Not yet implemented")
        }

        override fun positionX() {
            TODO("Not yet implemented")
        }

        override fun positionY() {
            TODO("Not yet implemented")
        }

    }
}