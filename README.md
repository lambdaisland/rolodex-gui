# Rolodex - Contact book GUI

In the [Lambda Island](https://lambdaisland.com) series on Ring you go step by step through the process of
building an API for a contact book application. Since an API by itself isn't
that much fun, there's this app that you can put in front of it to test if your
API is working.

## How to run

First make sure your API is up and running on localhost port 3000.

Clone this repo, then run the server that serves the web app with `lein run`.

Now open [http://localhost:3449](http://localhost:3449), and off you go.

## How to use

The buttons correspond 1-on-1 with the API operations implemented in the
episode, and are named according to the HTTP verb that they trigger. Hover over
them to get a better description of what they do.

## Proxy to API

This app is a good example of how to keep your front- and backend in separate
repositories, so they are decoupled and can be deployed separately. It proxies
to the API to get around cross-origin restrictions in browsers. Have a look at
`devserver.clj` to see how it's set up.
