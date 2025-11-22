package com.ramankumar.moviefinder.adapter

// Legacy RecyclerView adapter for the old XML-based movie list.
// The current app uses Jetpack Compose for all main screens, and the
// associated XML layout/resources (item_movie.xml, posterImageView, etc.)
// have been removed. To avoid unresolved reference build errors while
// keeping this code for potential future reuse, the implementation is
// commented out.

/*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.ramankumar.moviefinder.R
import com.ramankumar.moviefinder.model.Movie

class MovieAdapter(
    private val onMovieClick: (Movie) -> Unit,
    private val onFavoriteClick: (Movie) -> Unit
) : ListAdapter<Movie, MovieAdapter.MovieViewHolder>(MovieDiffCallback()) {

    init {
        setHasStableIds(true)
    }

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val posterImageView: ImageView = itemView.findViewById(R.id.posterImageView)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val yearTextView: TextView = itemView.findViewById(R.id.yearTextView)
        val ratingTextView: TextView = itemView.findViewById(R.id.ratingTextView)
        val favoriteButton: ImageButton = itemView.findViewById(R.id.favoriteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = getItem(position)

        holder.titleTextView.text = movie.title
        holder.yearTextView.text = movie.releaseDate.take(4)
        holder.ratingTextView.text = holder.itemView.context.getString(
            R.string.rating_format,
            movie.voteAverage
        )

        Glide.with(holder.itemView.context)
            .load(movie.getPosterUrl())
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.posterImageView)

        updateFavoriteIcon(holder.favoriteButton, movie.isFavorite)

        holder.itemView.setOnClickListener {
            onMovieClick(movie)
        }

        holder.favoriteButton.setOnClickListener {
            val updatedMovie = movie.copy(isFavorite = !movie.isFavorite)
            updateFavoriteIcon(holder.favoriteButton, updatedMovie.isFavorite)
            onFavoriteClick(updatedMovie)
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id?.toLong() ?: RecyclerView.NO_ID.toLong()
    }

    fun submitMovies(movies: List<Movie>) {
        submitList(movies)
    }

    private fun updateFavoriteIcon(button: ImageButton, isFavorite: Boolean) {
        if (isFavorite) {
            button.setImageResource(android.R.drawable.btn_star_big_on)
            button.setColorFilter(android.graphics.Color.parseColor("#FFD700"))
        } else {
            button.setImageResource(android.R.drawable.btn_star_big_off)
            button.setColorFilter(android.graphics.Color.parseColor("#E50914"))
        }
    }
}

private class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
    override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem == newItem
    }
}
*/
