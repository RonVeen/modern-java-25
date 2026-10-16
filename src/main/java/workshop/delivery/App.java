package workshop.delivery;

import workshop.delivery.domain.Address;
import workshop.delivery.domain.Dimensions;
import workshop.delivery.domain.Letter;
import workshop.delivery.domain.Parcel;
import workshop.delivery.domain.Shipment;
import workshop.delivery.domain.Weight;
import workshop.delivery.service.QuoteService;

import java.util.List;

public final class App {
    public static void main(String[] args) {
        var amsterdam = new Address("nl", "Amsterdam", "1012 JS");
        List<Shipment> shipments = List.of(
                new Letter("letter-17", amsterdam, new Weight(80)),
                new Parcel(
                        "parcel-21",
                        amsterdam,
                        new Dimensions(40, 30, 20),
                        new Weight(2_000)));

        var quoteService = new QuoteService();
        shipments.stream()
                .map(shipment -> quoteService.quote(shipment, "EXPRESS"))
                .forEach(System.out::println);
    }
}

