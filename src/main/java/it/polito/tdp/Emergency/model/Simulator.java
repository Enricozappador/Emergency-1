package it.polito.tdp.Emergency.model;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import it.polito.tdp.Emergency.model.Event.EventType;
import it.polito.tdp.Emergency.model.Paziente.CodiceColore;

public class Simulator {

	// PARAMETRI DI SIMULAZIONE
	private int NS = 5; // numero studi medici

	private int NP = 150; // numero di pazienti
	
	private int pazientitot; 
	private int pazientiDimessi; 
	private int pazientiAbbandonato; 
	private int pazientiMorti; 
	
	private List<Paziente> pazienti; 
	private PriorityQueue<Paziente> attesa; 
	private int studiLiberi ; 
	
	CodiceColore coloreAssegnato;
	
	private final Duration DURATION_TRIAGE = Duration.ofMinutes(5); 
	private final Duration DURATION_WHITE = Duration.ofMinutes(10); 
	private final Duration DURATION_YELLOW = Duration.ofMinutes(15); 
	private final Duration DURATION_RED = Duration.ofMinutes(30); 
	private Duration T_ARRIVAL = Duration.ofMinutes(5); // intervallo tra i pazienti
	
	private final Duration TIMEOUT_WHITE = Duration.ofMinutes(90); 
	private final Duration TIMEOUT_YELLOW = Duration.ofMinutes(30); 
	private final Duration TIMEOUT_RED = Duration.ofMinutes(60); 
	
	private final LocalTime oraInizio = LocalTime.of(8, 0); 
	private final LocalTime oraFine = LocalTime.of(20, 0);
	
	private final Duration TICK_TIME = Duration.ofMinutes(5);
	
	private PriorityQueue<Event> queue; 
	
	public void init() {
		this.queue = new PriorityQueue<>(); 
		this.pazienti = new ArrayList<>(); 
		this.attesa = new PriorityQueue<>(); 
		this.pazientitot = 0; 
		this.pazientiDimessi =0; 
		this.pazientiAbbandonato =0; 
		this.pazientiMorti = 0; 
		
		this.studiLiberi = this.NS; 
		
		
		this.coloreAssegnato = CodiceColore.WHITE; 
		
		int nPaz = 0; 
		LocalTime oraArrivo = this.oraInizio; 
		
		while(nPaz<this.NP && oraArrivo.isBefore(this.oraFine)) {
			Paziente p = new Paziente(oraArrivo, CodiceColore.UNKNOWN); 
			
			this.pazienti.add(p); 
			
			Event e = new Event(oraArrivo, EventType.ARRIVAL, p); 
			queue.add(e); 
			
			 
			
			
			nPaz++; 
			oraArrivo = oraArrivo.plus(T_ARRIVAL);
		}
		
		 
	}
	
	
	
	public void run() {
		
		while(!queue.isEmpty()) {
			Event e = this.queue.poll();
			System.out.println(e);
			processEvent(e); 
		}
		
	}
	
	
	private void processEvent(Event e) {
		
		Paziente paz = e.getPaziente(); 
		switch(e.getType()) {
		case ARRIVAL: 
			queue.add(new Event(e.getTime().plus(DURATION_TRIAGE),EventType.TRIAGE, e.getPaziente() )); 
			this.pazientitot++; 
			break; 
			
		case TRIAGE:
			paz.setColore(nuovoCodiceColore());
			
			attesa.add(paz); 
			
			
			if(paz.getColore()== CodiceColore.WHITE)
			queue.add(new Event(e.getTime().plus(TIMEOUT_WHITE), EventType.TIMEOUT, paz));
			else if(paz.getColore()== CodiceColore.YELLOW)
				queue.add(new Event(e.getTime().plus(TIMEOUT_YELLOW), EventType.TIMEOUT, paz));
			else if(paz.getColore()== CodiceColore.RED)
				queue.add(new Event(e.getTime().plus(TIMEOUT_RED), EventType.TIMEOUT, paz));
				
			
			break; 
		case FREE_STUDIO: 
			
			
			Paziente prossimo = attesa.poll();
			
			if(prossimo != null) {
				
				this.studiLiberi--; 
				
				if(paz.getColore()== CodiceColore.WHITE)
					queue.add(new Event(e.getTime().plus(DURATION_WHITE), EventType.TIMEOUT, paz));
					else if(paz.getColore()== CodiceColore.YELLOW)
						queue.add(new Event(e.getTime().plus(DURATION_YELLOW), EventType.TIMEOUT, paz));
					else if(paz.getColore()== CodiceColore.RED)
						queue.add(new Event(e.getTime().plus(DURATION_RED), EventType.TIMEOUT, paz));
						
			}
			break; 
		case TREATED: 
			this.studiLiberi++; 
			this.pazientiDimessi++; 
			
			this.queue.add(new Event (e.getTime(), EventType.FREE_STUDIO, null)); 
			break; 
		case TIMEOUT: 
			
			attesa.remove(paz); 
			
			switch(paz.getColore()) {
			case WHITE: 
				
				this.pazientiAbbandonato++; 
				break; 
			case YELLOW: 
				paz.setColore(CodiceColore.RED);
				attesa.add(paz); 
				break; 
			case RED: 
				
				this.pazientiMorti++; 
				break; 
				
			}
			
			
			break; 
		case TICK: 
			if(this.studiLiberi>0) {
				this.queue.add(new Event(e.getTime(), EventType.FREE_STUDIO, null)); 
				
			}
			this.queue.add(new Event(e.getTime().plus(this.TICK_TIME), EventType.TICK, null)); 
			break; 
			
		}
		
		
		
	}
	
	
	private CodiceColore nuovoCodiceColore() {
		CodiceColore nuovo = coloreAssegnato;
		
		if (coloreAssegnato == CodiceColore.WHITE) {
			coloreAssegnato = CodiceColore.YELLOW;
					}
		else if(coloreAssegnato == CodiceColore.YELLOW) {
			coloreAssegnato = CodiceColore.RED;
		}
		else
			coloreAssegnato = CodiceColore.WHITE;
		
		
		
		return nuovo;
		
		
	}



	public int getNS() {
		return NS;
	}
	public void setNS(int nS) {
		NS = nS;
	}

	
	
	
}
