package com.example

import com.example.cats.effect.HelloWorld
import munit.CatsEffectSuite
import org.specs2.matcher.Matchers

class HelloWorldSuite extends CatsEffectSuite with Matchers {

  test("test hello world says hi") {
    HelloWorld.say().map(it => assertEquals(it, "Hello Cats!"))
  }
}
