function celsiusToFahrenheit(celsius) {
  const fahrenheit = (celsius * 9/5) + 32;
  return parseFloat(fahrenheit.toFixed(1));
}

function fahrenheitToCelsius(fahrenheit) {
  const celsius = (fahrenheit - 32) * 5/9;
  return parseFloat(celsius.toFixed(3));
}

export default {
  celsiusToFahrenheit: celsiusToFahrenheit,
  fahrenheitToCelsius:fahrenheitToCelsius
}