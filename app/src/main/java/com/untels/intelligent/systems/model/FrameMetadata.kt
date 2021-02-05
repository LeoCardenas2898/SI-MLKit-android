package com.untels.intelligent.systems.model

class FrameMetadata private constructor(var width: Int, var height: Int, var rotation: Int) {

    /** Builder of [FrameMetadata].  */
    class Builder constructor(private var width: Int = 0, private var height: Int = 0, private var rotation: Int = 0) {
        fun setWidth(width: Int): Builder {
            this.width = width
            return this
        }

        fun setHeight(height: Int): Builder {
            this.height = height
            return this
        }

        fun setRotation(rotation: Int): Builder {
            this.rotation = rotation
            return this
        }

        fun build(): FrameMetadata = FrameMetadata(width, height, rotation)
    }
}