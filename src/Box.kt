package blake.nestedBox

class Box(private val nestAlignment: Char) {
    var parent:Box? = null
    private val children: MutableList<Box> = mutableListOf()
    var verticalPadding: Int = 1 // vertical size
    var horizontalPadding: Int = 1 // horizontal size
    var anchorX = 0 // X coord of top left corner
    var anchorY = 0 // Y coord of top left corner
    companion object GridObject { // Singleton shared by all instances of the class
        var grid = arrayOf<Array<Char>>() // 2d array of chars
        var initialized = false // flag for if the grid has been initialized (prevents the box from being cleared by nested boxes)
    }

    // Adds a Box to the children list
    fun nestBox(boxToNest: Box) {
        boxToNest.parent = this@Box // set parent to this instance of the object
        // Determine x, y of child
        if (children.count() == 0) { // if first child then determining child anchor is easy
            boxToNest.anchorX = anchorX + 2
            boxToNest.anchorY = anchorY + 1
        } else {
            val lastChild: Box = children.last()
            if (nestAlignment == 'h') { // horizontal alignment
                boxToNest.anchorY = anchorY + 1 // anchor Y is next line
                // anchor X is last child's X + last child's horizontal padding + 1 for space + 2
                boxToNest.anchorX = lastChild.anchorX + lastChild.horizontalPadding + 3
            } else { // vertical alignment
                boxToNest.anchorX = anchorX + 2 // 1 for space + 1
                // anchor Y is last child's Y + last child's vertical padding + 2
                boxToNest.anchorY = lastChild.anchorY + lastChild.verticalPadding + 2
            }
        }

        children.add(boxToNest) // add box to children
        // Make sure box needs to be adjusted
        var vPadding = verticalPadding
        var hPadding = horizontalPadding
        if (boxToNest.anchorY + boxToNest.verticalPadding < vPadding + anchorY) { vPadding = 0 } else { vPadding = boxToNest.verticalPadding }
        if (boxToNest.anchorX + boxToNest.horizontalPadding < hPadding + anchorX) { hPadding = 0 } else { hPadding = boxToNest.horizontalPadding }
        if (hPadding > 0 || vPadding > 0) { // only adjust if needed
            adjustPadding(hPadding, vPadding, children.count(), nestAlignment)
        }
    }

    // Prints the nested box structure to ascii art
    fun printGrid() {
        this@Box.parent?.let { // Ensure this is being called from the master box
            throw IllegalStateException("Print Must Be Called From the Master Box")
        }

        boxToGrid()
        val boxString = gridToString()

        print(boxString)
    }

    fun resetGrid() {
        grid = arrayOf<Array<Char>>()
        initialized = false
    }

    // Adjusts the padding of this box and the padding of all parent boxes
    private fun adjustPadding(hPadding: Int, vPadding: Int, childrenCount: Int, alignmentToUse: Char) {
        if (childrenCount == 1) { // Only adjust for the 1st element if it is not the stacking alignment
            when (alignmentToUse) {
                'h' -> verticalPadding += if (vPadding > 0) vPadding + 1 else 0
                'v' -> horizontalPadding += if (hPadding > 0) hPadding + 3 else 0 // 3 instead of 1 to account for horizontal padding between boxes
            }
        }
        when (alignmentToUse) {
            'v' -> verticalPadding += if (vPadding > 0) vPadding + if (childrenCount == 1) 1 else 2 else 0
            'h' -> horizontalPadding += if (hPadding > 0) hPadding + 3 else 0 // 3 instead of 1 to account for horizontal padding between boxes
        }
        // Make sure parent needs to be expanded
        val parentvPadding = parent?.verticalPadding ?: 0
        val parenthPadding = parent?.horizontalPadding ?: 0
        val parentAnchorX = parent?.anchorX ?: 0
        val parentAnchorY = parent?.anchorY ?: 0
        var vPadToParent = vPadding
        var hPadToParent = hPadding
        if (anchorY + verticalPadding < parentvPadding + parentAnchorY) { vPadToParent = 0 }
        if (anchorX + horizontalPadding < parenthPadding + parentAnchorX) { hPadToParent = 0 }
        this@Box.parent?.adjustPadding(hPadToParent, vPadToParent, childrenCount, alignmentToUse) // null safe call to adjustPadding of the parent
    }

    // Converts the nested box structure to the grid
    private fun boxToGrid() {
        if (!initialized) {
            initGrid()
        }

        children.forEach { it.boxToGrid() } // Call for each child box

        for (i in 0..verticalPadding + 1) {
            for (j in 0..horizontalPadding + 1) {
                val currentChar = getCharType(j, i)
                if (currentChar != ' ') { // don't draw spaces so nested boxes are not overwritten
                    grid[i + anchorY][j + anchorX] = currentChar
                }
            }
        }
    }

    // Returns the character type for an x, y coord
    private fun getCharType(x: Int, y: Int): Char {
        return if (isCorner(x, y))
            '+'
        else if (isHorizontalEdge(y))
            '-'
        else if (isVerticalEdge(x))
            '|'
        else
            ' '
    }

    // Checks if an x, y is a corner
    private fun isCorner(x: Int, y: Int): Boolean {
        // corners at 0 or padding + 1
        val cornerX = horizontalPadding + 1
        val cornerY = verticalPadding + 1
        // If both x, y = 0 or cornerX/Y it is a corner, any combination is valid
        return ((x == 0 && y == 0) || (x == 0 && y == cornerY) || (x == cornerX && y == 0) || (x == cornerX && y == cornerY))
    }

    // Checks if an x is a vertical edge
    private fun isVerticalEdge(x: Int): Boolean {
        val edgeX = horizontalPadding + 1
        return x == 0 || x == edgeX
    }

    // Checks if a y is a horizontal edge
    private fun isHorizontalEdge(y: Int): Boolean {
        val edgeY = verticalPadding + 1
        return y == 0 || y == edgeY
    }

    // Fills the 2d array with spaces
    private fun initGrid() {
        for (i in 0..verticalPadding + 1) {
            var array = arrayOf<Char>()
            for (j in 0..horizontalPadding + 1) {
                array += ' '
            }
            grid += array
        }
        initialized = true
    }

    // Converts the grid to a string
    private fun gridToString(): String {
        var boxString = ""

        for (i in 0..verticalPadding + 1) {
            for (j in 0..horizontalPadding + 1) {
                boxString += grid[i][j]
            }
            boxString += "\n"
        }

        return boxString
    }
}