package com.pradhi.kotlin

import io.vertx.core.Vertx
import io.vertx.core.json.Json

import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import kotlinx.serialization.Serializable
import kotlinx.serialization.list
import kotlinx.serialization.parse
import kotlin.Exception

fun main(args: Array<String>) {
    println("Hello, World")

    var flag = 0
    // creating list of objects for person
    val personMutableList : MutableList<Person> = mutableListOf<Person>(
        Person(1, "Pradhi"),
        Person(2, "Priya"),
        Person(3, "Mithrah")
    )

    //vertx creation and routing configuration
    val vertx = Vertx.vertx()
    val httpServer = vertx.createHttpServer()
    val router = Router.router(vertx)
     router.route().handler(BodyHandler.create())
    router.get("/")
        .handler({ routingContext ->
        val response = routingContext.response()
        response.putHeader("content-type","text/plain")
                 .setChunked(true)
                 .write("hi\n")
                 .end("ended")

    })

    // the person endpoint which displays all the person data

    router.get("/person")
        .handler({ routingContext ->
            val response = routingContext.response()
               response.putHeader("content-type","application/json")
                .setChunked(true)
                .write(kotlinx.serialization.json.Json.stringify(Person.serializer().list, personMutableList))
                .end()

        })

    //the person endpoint which displays the specific person
    router.get("/person/:id")
        .handler({ routingContext ->
            val request = routingContext.request()
            val personid: Int = Integer.valueOf(request.getParam("id"))
            val response = routingContext.response()



            for(personobj in personMutableList)
            {
                println("entered for"+personobj+"id"+personid)
                if(personobj.id==(personid))
                {
                    println("entered if"+personobj+""+personid)
                    flag = 1
                    response.putHeader("content-type","application/json")
                        .setChunked(true)
                        .setStatusCode(200)
                        .write(kotlinx.serialization.json.Json.stringify(Person.serializer(),personobj))
                        .end()
                }
                println("exited for")
            }


                 if(flag==1) {
                     response.putHeader("content-type", "text/plain")
                         .setChunked(true)
                         .setStatusCode(404)
                         .end("")
                 }

        })

    // for learning to fetch specific name and send the response
    router.get("/json/:name")
        .handler({ routingContext ->
            val request = routingContext.request()
            val personname:String = request.getParam("name")

            val response = routingContext.response()

             response.putHeader("content-type","application/json")
            .setChunked(true)
            .write(Json.encodePrettily(Person(1, personname)))
            .end("ended")

    })

    //Failure handling

    router.get("/person/cal/throw")
        .handler({ routingContext ->
            throw java.lang.RuntimeException("Something went wrong")
        })

    router.get("/person/calculation/fail")
        .handler({ routingContext ->
            routingContext.fail(403)
        })
    router.get("/person/*").failureHandler(
        { failureRoutingContext ->
            var failstatus = failureRoutingContext.statusCode()
            var failresponse = failureRoutingContext.response()
            failresponse.setStatusCode(failstatus)
                .end("sorry! some issue in calcultion")

        })

    //to post the person

    router.post("/person").consumes("*/json")
        .handler({ routingContext ->
              val response = routingContext.response()
            println("came to post")
            try {
                val person = kotlinx.serialization.json.Json.parse<Person>(Person.serializer(),routingContext.bodyAsString)
                println(person)
                val idcheck = personMutableList.map {it}.distinct().filter { it.id == person.id}

                if (idcheck.isNotEmpty())
                    response.setStatusCode(409).end("ID is already Present ")
                else
                    personMutableList.add(person)
                 response.setStatusCode(201).end()
                println("inside try")
            }catch (e: Exception ){
                e.printStackTrace()
                response.setChunked(true).setStatusCode(400).end(e.message)
            }


        })


    httpServer.requestHandler(router::accept).listen(8091)

}

//using kotlin serialization
@Serializable
data class Person(var id : Int ,var name : String = "")
