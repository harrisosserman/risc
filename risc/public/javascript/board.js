// This code inside of the boardViewModel function is only loaded once the game is fully initialized
(function() {
    function boardViewModel(context) {
        var self = context;
        self.territoryInfo = {};
        self.getMap = function() {
            $(".displayPlayerColor").each(function(index) {
                $(this).append(self.colorList[index]);
            });
            $.ajax('/test/game/' + self.gameID + '/map', {
                method: 'GET',
                    }).done(function(result) {
                        self.territoryInfo = $.parseJSON(result);
                        $("#map td").each(function(index) {
                            $(this).addClass("player" + self.territoryInfo.map[index].owner);
                            if(self.territoryInfo.map[index].owner === self.playerNumber) {
                                $(this).hover(function() {
                                    $(this).addClass("territoryHover");
                                }, function() {
                                    $(this).removeClass("territoryHover");
                                });
                            }

                            $(this).append("<p>troops: " + self.territoryInfo.map[index].troops + "</p>");
                        });
                });
        };
        self.getMap();
    }
    window.Board = boardViewModel;
})(window.ko);

