Core Matching Engine Order Book algorithm that processes orders 1 at a time. This project demos financial trading core domain knowledge and my code organization and readability, OO patterns, Data Structures and Algorithm, and the importance of Unit Testing.

The program reads from two input CSV files that you can find in `src/main/resources`:
- symbols
- orders

A symbol may be halted, in which case, do not accept any orders for that symbol.

For symbols that are not halted, order have all required fields for the order to be accepted.

An order can be one of two sides:  
- buy
- sell

There are two types of orders:
- Limit orders will try to execute at or better than the price defined (lower is better for buyers and higher is better for sellers). All fields are required. If the order does not match, it will sit on the order book waiting to be matched.
- Market orders will execute at any price. All fields except price is required. If a price is set, please ignore it. If the order does not match, it will be rejected with the reason that there was no match.

If an order matches with another order even partially, a trade is made. A trade consists of: symbol, price, timestamp

Produce 3 output files:
- trades.txt - contains all trades that were made
- rejected.txt - contains all rejected orders and their rejected reasons
- orderbook.txt - contains all orders that were not traded by the end

