# zanbai TODOs

* Functionality (server-side)
  * implement conversations and message exchange
  * check for validity of usernames when logging in
  * update user list when user closes tab

* Functionality (client-side)
  * allow selecting users and starting a conversation
  * update current conversations as well as pending messages
  * add users to conversation
  * close conversations
  * display error messages

* UI
  * enable login button only when input field contains a valid username
  * conversations should be [sortable](https://jsfiddle.net/1064q7jm/472/)
  * choose some nice typefaces, e.g.:
    * [Amaranth](https://fonts.google.com/specimen/Amaranth)
    * [Asap](https://fonts.google.com/specimen/Asap)
    * [Asul](https://fonts.google.com/specimen/Asul)
    * [Rosario](https://fonts.google.com/specimen/Rosario)
  * complete color scheme; so far:
    * light amber (#ffb300)
    * khaki
    * bootstrap primary blue

* Tests

* Future ideas
  * implement persistent storage
    * client-side: e.g. [storage-atom](https://github.com/alandipert/storage-atom) or [Plato](https://github.com/eneroth/plato) (also look at [re-frame-storage](https://github.com/akiroz/re-frame-storage))
    * server-side: maybe [enduro](https://github.com/alandipert/enduro)?
  * write spec for `app-db` using [clojure.spec](http://clojure.org/guides/spec) (as in [todomvc](https://github.com/Day8/re-frame/blob/master/examples/todomvc/src/todomvc/db.cljs))
  * more idiomatic client/server communication, using e.g. [Sente](https://github.com/ptaoussanis/sente) (also check out [Rente](https://github.com/enterlab/rente))
  * basically, look at [everything awesome in ClojureScript](https://github.com/hantuzun/awesome-clojurescript)
  * have the server app read a configuration file (e.g. for DB connection string)
  * signed cookies (for username)?
