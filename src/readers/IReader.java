package readers;

import java.util.List;

public interface IReader<D> {
	public List<D> read(String filename);		// Lecture de l'ensemble du jeu de donnees en une seule passe
	public void open(String filename);			// Ouverture de la source de donnees mais pas de lecture
	public void reset();						// Remise a zero du flux de donnees pour une nouvelle lecture
	public void close();						// Fermeture de la source de donnees
	public void next(int n);					// Lecture des n points suivants du jeu de donnees s'ils existent
	public List<String> getLabels();			// Renvoie la liste des etiquettes du jeu de donnees actuel
	public List<D> getDataSet();				// Renvoie la liste des etiquettes du jeu de donnees actuel
	public int count();							// Renvoie le nombre d'elements dans le lot qui a ete lu
	public void start();						// Remet le curseur de lecture au debut du flux
	public int size();							// Renvoie le nombre total d'elements dans le flux obtenu apres un appel a open() ou read()
	
}
