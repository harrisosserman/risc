// This code inside of the boardViewModel function is only loaded once the game is fully initialized
(function() {
    function boardViewModel(context) {
        var self = context;
        self.territoryInfo = {};
        self.getMap = function() {
            $.ajax('/test/game/' + self.gameID + '/map', {
                method: 'GET',
                    }).done(function(result) {
                        self.territoryInfo = $.parseJSON(result);
                        $("#map td").each(function(index) {
                            // $(this).attr("data-bind", "css: {playerList()[" + index + "]}");
                            $(this).addClass("player" + self.territoryInfo.map[index].owner);
                        });
                });
        };
        self.getMap();
    }
    window.Board = boardViewModel;
})(window.ko);

