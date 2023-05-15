# Marbloids
Marbloids is a simple falling marbles game in which you play against Android to score as many points as possible.
 
When it's your turn press a column on the playing grid, a new marble will appear at the lowest available position in that column. Android will do the same and add one of its marbles. This is repeated until there are no more spaces on the board at which point the game will end.
 
You score points by adding your marbles to form groups of three (horizontally, vertically or diagonally). When you create such a group the three marbles are removed from the grid and you score 100 points. Any marbles above the removed group will fall to occupy the newly vacant spaces. If these form new groups they too will be removed and additional points scored (100 for every group removed). This is repeated until all the possible groups have been removed. This 'cascade' effect can be used to clear away several marbles in a single move (and score lots of points!)
 
Your task is to clear away as many marbles as possible to score points and create space on the grid for more marbles to be added. Android's task is to stop you by adding marbles of its own, preventing you from creating your groups.

Android's marbles are added in exactly the same way but do not form groups of three. Instead they are removed only when they form a complete line across the playing grid. When Android marbles are removed any groups of three formed by your marbles will also be removed scoring further points for you.
