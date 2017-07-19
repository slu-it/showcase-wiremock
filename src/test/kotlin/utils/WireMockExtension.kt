package utils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.VerificationException
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.junit.jupiter.api.extension.*
import org.junit.jupiter.api.extension.ExtensionContext.Namespace

/**
 * This extension was implemented because WireMock, as of 2017-07-19, has no official JUnit 5 support.
 */
class WireMockExtension : BeforeAllCallback, BeforeEachCallback, AfterTestExecutionCallback, AfterEachCallback, ParameterResolver {

    private val namespace = Namespace.create("wiremock")

    override fun beforeAll(context: ContainerExtensionContext) {
        val server = WireMockServer(wireMockConfig().dynamicPort())
        setServer(context, server)
    }

    override fun beforeEach(context: TestExtensionContext) {
        val server = getServer(context).apply { start() }
        WireMock.configureFor("localhost", server.port())
    }

    override fun resolve(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        return getServer(extensionContext)
    }

    override fun supports(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return parameterContext.parameter.type == WireMockServer::class.java
    }

    override fun afterTestExecution(context: TestExtensionContext) {
        val server = getServer(context)
        val nearMisses = server.findNearMissesForAllUnmatchedRequests()
        if (nearMisses.isNotEmpty()) {
            throw VerificationException.forUnmatchedRequests(nearMisses)
        }
    }

    override fun afterEach(context: TestExtensionContext) {
        WireMock.reset()
        getServer(context).stop()
    }

    private fun setServer(context: ExtensionContext, server: WireMockServer) {
        getStore(context).put("server", server)
    }

    private fun getServer(context: ExtensionContext): WireMockServer {
        return getStore(context).get("server", WireMockServer::class.java)
    }

    private fun getStore(context: ExtensionContext): ExtensionContext.Store {
        return context.getStore(namespace)
    }

}