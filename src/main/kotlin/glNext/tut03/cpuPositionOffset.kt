package glNext.tut03

import com.jogamp.newt.event.KeyEvent
import com.jogamp.opengl.GL.*
import glm.glm
import com.jogamp.opengl.GL3
import glNext.*
import glm.set
import glm.vec._2.Vec2
import main.framework.Framework
import uno.buffer.*
import uno.glsl.programOf

/**
 * Created by GBarbieri on 21.02.2017.
 */

fun main(args: Array<String>) {
    CpuPositionOffset_Next().setup("Tutorial 03 - CPU Position Offset")
}

class CpuPositionOffset_Next : Framework() {

    var theProgram = 0
    val positionBufferObject = intBufferBig(1)
    val vao = intBufferBig(1)
    val vertexPositions = floatBufferOf(
            +0.25f, +0.25f, 0.0f, 1.0f,
            +0.25f, -0.25f, 0.0f, 1.0f,
            -0.25f, -0.25f, 0.0f, 1.0f)
    var startingTime = 0L

    override fun init(gl: GL3) = with(gl) {

        initializeProgram(gl)
        initializeVertexBuffer(gl)

        glGenVertexArray(vao)
        glBindVertexArray(vao)

        startingTime = System.currentTimeMillis()
    }

    fun initializeProgram(gl: GL3) {
        theProgram = programOf(gl, javaClass, "tut03", "standard.vert", "standard.frag")
    }

    fun initializeVertexBuffer(gl: GL3) = gl.initArrayBuffer(positionBufferObject) { data(vertexPositions, GL_STATIC_DRAW) }

    override fun display(gl: GL3) = with(gl) {

        val offset = Vec2(0f)

        computePositionOffsets(offset)
        adjustVertexData(gl, offset)

        clear { color() }

        usingProgram(theProgram) {
            withVertexLayout(positionBufferObject, glf.pos4) { glDrawArrays(3) }
        }
    }

    fun computePositionOffsets(offset: Vec2) {

        val loopDuration = 5.0f
        val scale = glm.PIf * 2.0f / loopDuration

        val elapsedTime = (System.currentTimeMillis() - startingTime) / 1_000f

        val fCurrTimeThroughLoop = elapsedTime % loopDuration

        offset.x = glm.cos(fCurrTimeThroughLoop * scale) * 0.5f
        offset.y = glm.sin(fCurrTimeThroughLoop * scale) * 0.5f
    }

    fun adjustVertexData(gl: GL3, offset: Vec2) = with(gl) {

        val newData = floatBufferBig(vertexPositions.capacity())
        repeat(vertexPositions.capacity()) { newData[it] = vertexPositions[it] }

        for (iVertex in 0 until vertexPositions.capacity() step 4) {

            newData[iVertex + 0] = vertexPositions[iVertex + 0] + offset.x
            newData[iVertex + 1] = vertexPositions[iVertex + 1] + offset.y
        }

        withArrayBuffer(positionBufferObject) { subData(newData) }

        newData.destroy()
    }

    override fun reshape(gl: GL3, w: Int, h: Int) = with(gl) {
        glViewport(w, h)
    }

    override fun end(gl: GL3) = with(gl) {

        glDeleteProgram(theProgram)
        glDeleteBuffer(positionBufferObject)
        glDeleteVertexArray(vao)

        destroyBuffers(positionBufferObject, vao, vertexPositions)
    }

    override fun keyPressed(keyEvent: KeyEvent) {

        when (keyEvent.keyCode) {
            KeyEvent.VK_ESCAPE -> quit()
        }
    }
}