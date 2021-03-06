package io.micronaut.docs.security.authentication

import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class UserControllerSpec extends Specification {
    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer, [
            "spec.name": "authenticationparam",
            "micronaut.security.enabled": true,
    ], "test")

    @Shared
    @AutoCleanup
    RxHttpClient client = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.getURL())

    def "verify you can use io.micronaut.security.authentication.Authentication as controller parameter to get the logged in user"() {
        when:
        HttpRequest request = HttpRequest.GET("/user/myinfo")
        HttpResponse<Map> rsp = client.toBlocking().exchange(request, Map)

        then:
        rsp.status() == HttpStatus.OK
        !rsp.body().containsKey('username')

        when:
        String username = 'user'
        String password = 'password'
        String encoded = "$username:$password".bytes.encodeBase64()
        String authorization = "Basic $encoded".toString()
        request = HttpRequest.GET("/user/myinfo").header("Authorization", authorization)
        rsp = client.toBlocking().exchange(request, Map)

        then:
        rsp.status() == HttpStatus.OK
        rsp.body().containsKey('username')
        rsp.body()['username'] == 'user'
        rsp.body()['roles'] == ['ROLE_USER']
    }

}
