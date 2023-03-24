import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from 'src/environments/environment';
import {Horse, HorseSearch} from '../dto/horse';
import {Sex} from '../dto/sex';

const baseUri = environment.backendUrl + '/horses';

@Injectable({
  providedIn: 'root'
})
export class HorseService {

  constructor(
    private http: HttpClient,
  ) { }

  /**
   * Get all horses stored in the system
   *
   * @return observable list of found horses.
   */
  getAll(): Observable<Horse[]> {
    return this.http.get<Horse[]>(baseUri);
  }


  /**
   * Create a new horse in the system.
   *
   * @param horse the data for the horse that should be created
   * @return an Observable for the created horse
   */
  create(horse: Horse): Observable<Horse> {
    return this.http.post<Horse>(
      baseUri,
      horse
    );
  }

  /**
   * Update an existing horse
   *
   * @param horse the data for the horse that should be updated
   * @return an Observable for the updated horse
   */
  update(horse: Horse): Observable<Horse> {
    return this.http.put<Horse>(
      `${baseUri}/${horse.id}`,
      horse
    );
  }

  /**
   * Get a horse by its id
   *
   * @param id the id of the horse
   * @return an Observable for the horse
   */
  get(id: number): Observable<Horse> {
    return this.http.get<Horse>(`${baseUri}/${id}`);
  }

  /**
   * Delete a horse by its id
   *
   * @param id the id of the horse to delete
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${baseUri}/${id}`);
  }

  search(searchData: HorseSearch = {}): Observable<Horse[]> {

    /* iterate through object and add params typesafe */
    let params = new HttpParams();
    Object.keys(searchData).forEach(key => {
      const objKey = key as keyof HorseSearch;
      const objVal = searchData[objKey];

      if (objVal !== undefined) {
        params = params.append(objKey, objVal);
      }
    });

    return this.http.get<Horse[]>(baseUri, { params });
  }
}
