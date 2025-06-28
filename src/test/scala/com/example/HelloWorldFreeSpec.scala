package com.example

import com.example.cats.effect.HelloWorld
import _root_.cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

class HelloWorldFreeSpec extends AsyncFreeSpec, AsyncIOSpec, Matchers:

  "examples" - {
    "should say hello" in {
      HelloWorld.say().map(_ shouldBe "Hello Cats!")
    }
  }
