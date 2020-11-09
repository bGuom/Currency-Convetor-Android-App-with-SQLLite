# Currency-Convetor-Android-App-with-SQLLite

Android app for foreign currency conversion. This app support features like 

#### Multi language
#### searching
#### Saving favourite currencies using SQLLite
#### Saving search history using sharedpreferences
#### Async requests for fetching conversion rates


User can enter a number, and convert that number from one currency to another. 

Conversion rates are fetched using exchangeratesapi 

For example : 

List supported currencies by API : 
```
https://api.exchangeratesapi.io/latest
```
Convert USD to EUR and GBP: 
```
https://api.exchangeratesapi.io/latest?base=USD&symbols=EUR,GBP
```

The user can save these conversions to a list of favourites.
The user can search for conversion rates for various currencies.
The user can save the conversion type (USD – EUR), or (CAD – GBP) for later viewing in a favourites list. 
The can remove items from the list and database.
Application saves the last conversion (currency, and amount) that was searched and displays the next time the application is launched. 

