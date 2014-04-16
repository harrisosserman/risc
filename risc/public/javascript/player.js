(function(ko) {
    function playerViewModel(globals) {
        var globalFunctions = globals;
        var player = this;
        player.username = ko.observable();
        player.password = ko.observable();
        player.passwordCheck = ko.observable();
        player.displaySignupSigninModal = ko.observable(true);
        player.displayGameStart = ko.observable(true);
        player.displayLogin = ko.observable(true);
        player.displaySignup = ko.observable(false);
        player.usernameError = ko.observable(false);
        player.passwordError = ko.observable(false);
        player.loginError = ko.observable(false);
        player.playerNumber = -1;
        player.hasLoadedChat = false;
        player.displayChat = ko.observable(false);
        player.chatIntervalId = 0;

        globalFunctions.getUsername = function() {
            return player.username();
        };
        globalFunctions.setPlayerNumber = function(number) {
            player.playerNumber = number;
        };
        globalFunctions.getPlayerNumber = function() {
            return player.playerNumber;
        };
        globalFunctions.setDisplayChat = function(input) {
            player.displayChat(input);
            clearInterval(player.chatIntervalId);
            player.chatIntervalId = 0;
            $("#openChatButton").removeClass('alertNewMessage');
        };
        globalFunctions.showGameLobby = function() {
            player.displaySignupSigninModal(false);
            player.displayLogin(false);
            player.loginError(false);
            globalFunctions.setDisplayGameLobby(true);
            globalFunctions.setDisplayMap(false);
        };
        globalFunctions.showGameSigninSignup = function() {
            player.displaySignupSigninModal(true);
            player.displayGameStart(true);
            player.displayLogin(true);
            player.displaySignup(false);
            player.usernameError(false);
            player.passwordError(false);
            player.loginError(false);

        };
        player.usernameChanged = function() {
            $.ajax('/test/player/' + player.username(), {
                method: 'GET'
            }).fail(function() {
                //there is no username in the db with the username the user tried to use
                player.usernameError(false);
            }).done(function() {
                player.usernameError(true);
            });

        };
        player.closeChat = function() {
            player.displayChat(false);
        };
        player.newChatUpdate = function() {
            // console.log("new chat update");
            if(player.chatIntervalId === 0) {
                player.chatIntervalId = setInterval(function() {
                    $("#openChatButton").toggleClass('alertNewMessage');
                }, 500);
            }

        };
        player.loadChat = function() {
           if(player.hasLoadedChat === false) {
                var chatRef = new Firebase('https://torid-fire-6946.firebaseio.com');
                var chat = new FirechatUI(chatRef, document.getElementById("firechat-wrapper"));
                // chat._chat.createRoom("RISC12345", "public", function(roomId) {
                //      chat._chat.getRoom(roomId, function(room) {
                //             console.log(room);
                //         });
                // });

                var simpleLogin = new FirebaseSimpleLogin(chatRef, function(err, user) {
                  if (user) {
                    chat.setUser(user.id, globalFunctions.getUsername());
                    chat.on('room-invite', function() {
                        player.newChatUpdate();
                    });
                    chat.on('message-add', function() {
                        player.newChatUpdate();
                    });

                    setTimeout(function() {
                      chat._chat.enterRoom('-JJzwl7SG5J01akn17WU');
                    }, 500);
                  } else {
                    simpleLogin.login('anonymous');
                  }
                });
                player.hasLoadedChat = true;
            }
        };
        player.login = function() {
            $.ajax('/test/player/' + player.username() + '/login', {
                method: 'POST',
                data: JSON.stringify({
                    'name': player.username(),
                    'password': CryptoJS.SHA512(player.password()).toString()
                }),
                contentType: "application/json"
            }).done(function() {
                player.loadChat();
                globalFunctions.showGameLobby();
            }).fail(function() {
                player.loginError(true);
            });
        };
        player.showSignup = function() {
            player.displaySignup(true);
            player.displayLogin(false);
        };
        player.createUser = function() {
            if(player.password() !== player.passwordCheck()) {
                player.passwordError(true);
                return;
            }
            player.passwordError(false);

            $.ajax('/test/player', {
                method: 'POST',
                data: JSON.stringify({
                    'name': player.username(),
                    'password': CryptoJS.SHA512(player.password()).toString()
                }),
                contentType: "application/json"
            }).done(function() {
                player.displaySignupSigninModal(false);
                player.displayLogin(false);
                player.usernameError(false);
                globalFunctions.setDisplayGameLobby(true);
                player.loadChat();
                //do a get to the username
            }).fail(function() {
                player.usernameError(true);
            });
        };

        ko.applyBindings(this, document.getElementById('playerKnockout'));
    }
    window.Player = playerViewModel;
})(window.ko);